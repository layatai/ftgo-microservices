# Saga Pattern Implementation Guide

## Overview

This document provides a comprehensive guide to the orchestration-based saga implementation in the FTGO microservices application. The saga pattern is used to manage distributed transactions across multiple services when creating an order.

## Architecture

### Orchestration-Based Saga

The application uses an **orchestration-based saga** pattern where a central coordinator (`SagaManager`) manages the entire transaction flow. This differs from choreography-based sagas where services coordinate through events.

### Key Components

#### 1. SagaManager (`ftgo-order-service/saga/SagaManager.java`)

**Role**: Central orchestrator that coordinates all saga steps

**Responsibilities**:
- Creates saga instances
- Executes steps sequentially
- Handles step results and failures
- Triggers compensation on failure
- Manages saga state

**Key Methods**:
- `createSagaInstance()` - Creates and starts a saga
- `executeNextStep()` - Executes the next step in sequence
- `handleStepResult()` - Processes successful step completion
- `handleStepFailure()` - Handles step failures and triggers compensation
- `compensateSaga()` - Executes compensating transactions

#### 2. SagaDefinition (`ftgo-order-service/saga/SagaDefinition.java`)

**Role**: Interface for defining saga structure

**Implementations**:
- `CreateOrderSagaDefinition` - Defines the Create Order Saga

**Key Methods**:
- `getSagaType()` - Returns saga type identifier
- `getSteps()` - Returns ordered list of steps
- `getNextStep()` - Gets the next step to execute

#### 3. AsyncSagaStep (`ftgo-order-service/saga/AsyncSagaStep.java`)

**Role**: Interface for async saga steps

**Key Methods**:
- `getName()` - Returns step name
- `executeAsync()` - Executes step asynchronously with callback
- `hasCompensation()` - Indicates if step has compensation
- `compensateAsync()` - Executes compensating transaction

**Step Implementations**:
- `ValidateOrderStep` - Validates order
- `CreateTicketStep` - Creates kitchen ticket via REST
- `AuthorizeCardStep` - Authorizes payment via REST
- `ConfirmCreateOrderStep` - Confirms order creation

#### 4. SagaInstance (`ftgo-order-service/saga/model/SagaInstance.java`)

**Role**: Represents a saga execution instance

**State Management**:
- `STARTED` - Saga created, first step about to execute
- `IN_PROGRESS` - Steps executing
- `COMPLETED` - All steps succeeded
- `FAILED` - A step failed
- `COMPENSATING` - Compensation in progress
- `COMPENSATED` - Compensation completed

**Persistence**:
- Stored in `saga_instances` table
- Includes saga data (JSON serialized)
- Tracks step executions in `saga_step_executions` table

## Saga Flow

### Create Order Saga Steps

1. **ValidateOrderStep**
   - **Action**: Validates order and approves it
   - **Service**: Order Service (local)
   - **Compensation**: Rejects the order
   - **Retry**: Yes (transient failures)

2. **CreateTicketStep**
   - **Action**: Creates kitchen ticket
   - **Service**: Kitchen Service (REST call)
   - **Compensation**: Cancels the ticket (DELETE /tickets/{ticketId})
   - **Retry**: Yes (network errors, 5xx)
   - **Result**: Stores `ticketId` in saga data

3. **AuthorizeCardStep**
   - **Action**: Authorizes customer payment
   - **Service**: Accounting Service (REST call)
   - **Compensation**: Releases authorization
   - **Retry**: Yes (network errors, 5xx)
   - **Result**: Stores `paymentId` in saga data

4. **ConfirmCreateOrderStep**
   - **Action**: Confirms order creation
   - **Service**: Order Service (local)
   - **Compensation**: None (final step)
   - **Retry**: Yes

### Execution Flow

```
1. OrderService.createOrder()
   ↓
2. OrderSagaService.createOrderSaga()
   ├─→ IdempotencyHandler.checkIdempotency()
   ├─→ SemanticLockManager.acquireLock()
   └─→ SagaManager.createSagaInstance()
       ↓
3. SagaManager.executeNextStep()
   ├─→ Find CreateOrderSagaDefinition
   ├─→ Get next step (ValidateOrderStep)
   ├─→ Update saga state to IN_PROGRESS
   └─→ SagaStepRetryHandler.executeWithRetry()
       ↓
4. Step Execution (with Resilience4j Retry)
   ├─→ step.executeAsync(sagaData, callback)
   ├─→ Step performs action (local or REST)
   └─→ Reports result via callback
       ↓
5. Callback Handling
   ├─→ Success → handleStepResult() → Next step
   └─→ Failure → handleStepFailure() → Compensation
```

### Compensation Flow

When a step fails:

```
1. handleStepFailure() called
   ↓
2. Saga state set to FAILED
   ↓
3. compensateSaga() called
   ↓
4. For each completed step (reverse order):
   ├─→ If step.hasCompensation() == true
   ├─→ step.compensateAsync(sagaData, callback)
   └─→ Compensation executed
   ↓
5. Saga state set to COMPENSATED
```

## Retry Mechanism

### Resilience4j Retry Integration

**Configuration** (`application.yml`):
```yaml
resilience4j:
  retry:
    configs:
      default:
        maxRetryAttempts: 3
        waitDuration: 1s
        exponentialBackoffMultiplier: 2
        maxWaitDuration: 30s
        retryExceptions:
          - java.net.ConnectException
          - java.net.SocketTimeoutException
          - org.springframework.web.reactive.function.client.WebClientException
```

**How It Works**:
1. `SagaStepRetryHandler` wraps step execution in `CompletableFuture`
2. Resilience4j Retry decorates the CompletableFuture
3. On failure, retries with exponential backoff: 1s, 2s, 4s, 8s (capped at 30s)
4. Only retries configured exceptions (network errors, 5xx)
5. After max retries, reports failure to saga manager

**Retry Flow**:
```
Attempt 1 → Failure → Wait 1s → Attempt 2
Attempt 2 → Failure → Wait 2s → Attempt 3
Attempt 3 → Failure → Wait 4s → Attempt 4
Attempt 4 → Failure → Report to SagaManager → Compensation
```

## Idempotency

### Implementation

**Location**: `ftgo-order-service/saga/IdempotencyHandler.java`

**How It Works**:
1. Client sends `Idempotency-Key` header with order creation request
2. `IdempotencyHandler` checks if saga exists for this key
3. If exists, returns existing saga instance
4. If not, creates new saga and records the key

**Database**:
- `saga_instances.idempotency_key` column (unique index)
- Prevents duplicate saga creation

**Usage**:
```java
// In OrderController
@RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey

// In OrderSagaService
if (sagaData.getIdempotencyKey() != null) {
    Optional<SagaInstance> existing = idempotencyHandler.checkIdempotency(key);
    if (existing.isPresent()) {
        return existing.get(); // Return existing saga
    }
}
```

## Semantic Locking

### Implementation

**Location**: `ftgo-order-service/saga/SemanticLockManager.java`

**Technology**: Redis (distributed locking)

**How It Works**:
1. Before creating saga, acquire lock on resource (e.g., Order)
2. Lock key: `saga:lock:Order:{orderId}`
3. Lock value: `sagaInstanceId`
4. Lock expiration: 30 minutes (matches saga timeout)
5. On saga completion/failure, release lock

**Lock Operations**:
- `acquireLock()` - Uses Redis SET with NX (set if not exists) and EX (expiration)
- `releaseLock()` - Uses Lua script to atomically check and delete
- `releaseAllLocks()` - Releases all locks for a saga instance

**Benefits**:
- Prevents concurrent modifications during saga execution
- Handles lack of isolation in distributed transactions
- Automatic expiration prevents deadlocks

## Timeout Handling

### Implementation

**Location**: `ftgo-order-service/saga/SagaTimeoutHandler.java`

**How It Works**:
1. Scheduled task runs every minute
2. Finds sagas in `IN_PROGRESS` state older than 30 minutes
3. Finds step executions older than 5 minutes
4. Triggers compensation for timed-out sagas

**Configuration**:
- Saga timeout: 30 minutes
- Step timeout: 5 minutes
- Check interval: 1 minute

## Saga Data Model

### CreateOrderSagaData

**Fields**:
- `sagaInstanceId` - ID of the saga instance
- `orderId` - Order being created
- `customerId` - Customer placing order
- `restaurantId` - Restaurant for order
- `ticketId` - Created ticket ID (from CreateTicketStep)
- `paymentId` - Payment authorization ID (from AuthorizeCardStep)
- `idempotencyKey` - Idempotency key
- `lineItems` - Order line items
- `orderTotal` - Total order amount

**Usage**:
- Passed to each step
- Updated with step results
- Used for compensation

## Database Schema

### saga_instances Table

```sql
CREATE TABLE saga_instances (
    id VARCHAR(255) PRIMARY KEY,
    saga_type VARCHAR(255) NOT NULL,
    saga_data TEXT NOT NULL,
    state VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    failure_reason TEXT,
    idempotency_key VARCHAR(255) UNIQUE
);
```

### saga_step_executions Table

```sql
CREATE TABLE saga_step_executions (
    id VARCHAR(255) PRIMARY KEY,
    saga_instance_id VARCHAR(255) NOT NULL,
    step_name VARCHAR(255) NOT NULL,
    state VARCHAR(50) NOT NULL,
    result TEXT,
    created_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    FOREIGN KEY (saga_instance_id) REFERENCES saga_instances(id) ON DELETE CASCADE
);
```

## Error Handling

### Retryable Errors

- Network errors: `ConnectException`, `SocketTimeoutException`
- Timeout errors: `TimeoutException`
- WebClient errors: `WebClientException`, `WebClientResponseException`
- HTTP 5xx errors: 500, 502, 503, 504

### Non-Retryable Errors

- Validation errors: `IllegalArgumentException`, `IllegalStateException`
- Business logic errors: `InvalidOperationException`
- Not found errors: `EntityNotFoundException`

### Failure Handling

1. **Step Failure**:
   - Retry (if retryable) up to max attempts
   - If retries exhausted → trigger compensation
   - Update saga state to FAILED

2. **Compensation Failure**:
   - Log error but continue with other compensations
   - Don't fail the compensation process
   - Saga state set to COMPENSATED

## Best Practices

### 1. Step Design

- Keep steps focused and atomic
- Steps should be idempotent (safe to retry)
- Store results needed for compensation
- Use REST for cross-service communication

### 2. Compensation Design

- Compensations should be idempotent
- Handle cases where resource doesn't exist
- Don't fail compensation if resource already compensated
- Log all compensation actions

### 3. Error Handling

- Distinguish retryable vs non-retryable errors
- Provide clear error messages
- Log all failures with context
- Use structured logging

### 4. Testing

- Test successful saga completion
- Test step failure scenarios
- Test compensation execution
- Test idempotency
- Test timeout handling
- Test retry logic

## Monitoring and Observability

### Metrics

- Saga creation count
- Saga completion count
- Saga failure count
- Step execution time
- Retry attempt count
- Compensation execution count

### Logging

- Saga lifecycle events (created, started, completed, failed)
- Step execution (started, completed, failed)
- Retry attempts
- Compensation execution
- Timeout detection

### Tracing

- Distributed tracing via Spring Cloud Sleuth
- Trace ID propagated across saga steps
- View complete saga flow in Zipkin

## Common Patterns

### Adding a New Saga Step

1. Create step class implementing `AsyncSagaStep`
2. Implement `executeAsync()` and `compensateAsync()`
3. Add step to `CreateOrderSagaDefinition.getSteps()`
4. Update `SagaManager.storeStepResult()` if step returns data
5. Add database migration if needed

### Adding a New Saga Type

1. Create new `SagaDefinition` implementation
2. Define steps for the saga
3. Register in `SagaManager` (auto-discovered via Spring)
4. Create saga data class
5. Add service method to initiate saga

## Troubleshooting

### Saga Stuck in IN_PROGRESS

- Check for step execution timeout
- Verify callback is being called
- Check logs for step execution
- Use timeout handler to detect and compensate

### Compensation Not Executing

- Verify `hasCompensation()` returns true
- Check compensation implementation
- Verify step was completed before failure
- Check logs for compensation errors

### Retry Not Working

- Verify exception is in retryExceptions list
- Check retry configuration
- Verify Resilience4j is properly configured
- Check logs for retry attempts

