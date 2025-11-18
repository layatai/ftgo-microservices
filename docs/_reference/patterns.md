# Microservices Patterns Reference

This document describes all the patterns implemented in the FTGO microservices application and how they are used.

## 1. Saga Pattern (Orchestration-Based)

### Purpose
Manage distributed transactions across multiple services without using 2PC (Two-Phase Commit).

### Implementation
- **Location**: `ftgo-order-service/saga/`
- **Orchestrator**: `SagaManager`
- **Pattern**: Centralized orchestration (vs. choreography)

### How It Works
1. SagaManager coordinates all steps sequentially
2. Each step executes and reports back via callback
3. On failure, compensating transactions execute in reverse order
4. Saga state persisted in database

### Benefits
- No distributed locks
- Better performance than 2PC
- Handles failures gracefully
- Centralized control and visibility

### Trade-offs
- Central orchestrator is a single point of coordination
- Requires orchestrator to be available
- More coupling than choreography

## 2. Compensating Transactions

### Purpose
Undo operations when a saga fails.

### Implementation
- Each saga step implements `compensateAsync()`
- Compensation executed in reverse order
- Idempotent compensation operations

### Example
```java
// CreateTicketStep creates ticket
// Compensation: Delete ticket
compensateAsync() {
    DELETE /tickets/{ticketId}
}
```

### Best Practices
- Compensations should be idempotent
- Handle cases where resource doesn't exist
- Don't fail compensation if already compensated

## 3. Idempotency

### Purpose
Handle duplicate requests gracefully.

### Implementation
- **Location**: `ftgo-order-service/saga/IdempotencyHandler.java`
- Uses idempotency keys from request headers
- Stores key in `saga_instances.idempotency_key`
- Returns existing saga if key matches

### Usage
```http
POST /api/orders
Idempotency-Key: unique-request-id-123
```

### Benefits
- Prevents duplicate processing
- Safe to retry failed requests
- Better user experience

## 4. Semantic Locking

### Purpose
Handle lack of isolation in distributed transactions.

### Implementation
- **Location**: `ftgo-order-service/saga/SemanticLockManager.java`
- **Technology**: Redis
- Locks resources during saga execution
- Automatic expiration (30 minutes)

### How It Works
1. Acquire lock before saga creation
2. Lock key: `saga:lock:{ResourceType}:{resourceId}`
3. Lock value: `sagaInstanceId`
4. Release lock on completion/failure

### Benefits
- Prevents concurrent modifications
- No database-level locks
- Automatic cleanup via expiration

## 5. Event Sourcing (Partial)

### Purpose
Publish domain events for eventual consistency.

### Implementation
- Domain events stored in entities (transient)
- Events published to Kafka
- Other services consume events

### Events Published
- `OrderCreatedEvent`
- `OrderApprovedEvent`
- `TicketReadyEvent`
- `DeliveryDeliveredEvent`
- etc.

### Benefits
- Audit trail
- Eventual consistency
- Loose coupling

### Note
Full event sourcing (event store as source of truth) not implemented. Events are side effects of state changes.

## 6. API Composition

### Purpose
Aggregate data from multiple services.

### Implementation
- **Location**: `ftgo-api-gateway`
- API Gateway routes and composes requests
- Single entry point for clients

### Benefits
- Simplified client interface
- Hides service complexity
- Centralized cross-cutting concerns

## 7. Service Discovery

### Purpose
Enable services to find each other dynamically.

### Implementation
- **Technology**: Consul
- Services register on startup
- Health checks every 10 seconds
- Service-to-service communication uses service names

### Configuration
```yaml
spring:
  cloud:
    consul:
      host: localhost
      port: 8500
      discovery:
        enabled: true
```

### Benefits
- No hardcoded service URLs
- Automatic load balancing
- Health-aware routing

## 8. Circuit Breaker

### Purpose
Prevent cascading failures.

### Implementation
- **Technology**: Resilience4j
- Applied to external service calls
- Opens circuit on high failure rate
- Half-open state for recovery testing

### Configuration
```yaml
resilience4j:
  circuitbreaker:
    instances:
      restaurant-service:
        failureRateThreshold: 50
        waitDurationInOpenState: 10s
```

### States
- **Closed**: Normal operation
- **Open**: Failing fast, not calling service
- **Half-Open**: Testing if service recovered

## 9. Retry with Exponential Backoff

### Purpose
Handle transient failures automatically.

### Implementation
- **Technology**: Resilience4j Retry
- Applied to saga step execution
- Exponential backoff: 1s, 2s, 4s, 8s (capped at 30s)
- Max retries: 3 attempts

### Configuration
```yaml
resilience4j:
  retry:
    configs:
      default:
        maxRetryAttempts: 3
        waitDuration: 1s
        exponentialBackoffMultiplier: 2
        maxWaitDuration: 30s
```

### Retryable Errors
- Network errors
- Timeouts
- HTTP 5xx errors

### Benefits
- Automatic recovery from transient failures
- Reduces manual intervention
- Better user experience

## 10. Database per Service

### Purpose
Ensure service autonomy and data isolation.

### Implementation
- Each service has its own database
- No shared databases
- Services own their data completely

### Databases
- `ftgo_customer`
- `ftgo_restaurant`
- `ftgo_order`
- `ftgo_kitchen`
- `ftgo_delivery`
- `ftgo_accounting`

### Benefits
- Service autonomy
- Independent scaling
- Technology diversity possible
- Clear data ownership

### Trade-offs
- Distributed data management
- Eventual consistency
- More complex queries across services

## 11. Timeout Handling

### Purpose
Detect and handle long-running or stuck sagas.

### Implementation
- **Location**: `ftgo-order-service/saga/SagaTimeoutHandler.java`
- Scheduled task runs every minute
- Detects sagas older than 30 minutes
- Detects steps older than 5 minutes
- Triggers compensation automatically

### Configuration
- Saga timeout: 30 minutes
- Step timeout: 5 minutes
- Check interval: 1 minute

### Benefits
- Prevents resource leaks
- Automatic cleanup
- Better reliability

## 12. Domain-Driven Design (DDD)

### Purpose
Organize code around business domains.

### Implementation
- Domain entities with business logic
- Application services for use cases
- Infrastructure for technical concerns
- Presentation for API layer

### Layers
1. **Domain**: Entities, value objects, domain logic
2. **Application**: Use cases, DTOs, application services
3. **Infrastructure**: JPA, events, external services
4. **Presentation**: REST controllers

### Benefits
- Clear separation of concerns
- Business logic in domain
- Testable architecture
- Maintainable code

## Pattern Interactions

### Saga + Retry
- Retry handles transient failures in saga steps
- Saga handles business logic failures with compensation

### Saga + Semantic Locking
- Locks prevent concurrent saga execution on same resource
- Locks released on saga completion/failure

### Saga + Idempotency
- Idempotency prevents duplicate saga creation
- Saga ensures idempotent step execution

### Event Sourcing + Service Discovery
- Events enable loose coupling
- Service discovery enables dynamic routing

### Circuit Breaker + Retry
- Retry handles transient failures
- Circuit breaker prevents cascading failures
- Circuit opens if retries consistently fail

## Pattern Selection Guide

### When to Use Saga Pattern
- Multi-step distributed transaction
- Need compensation on failure
- Can't use 2PC (performance, availability)

### When to Use Event Sourcing
- Need complete audit trail
- Eventual consistency acceptable
- Complex event history queries

### When to Use CQRS
- Read and write workloads differ significantly
- Need to scale reads independently
- Complex query requirements

### When to Use Circuit Breaker
- Calling external services
- Service may be unavailable
- Need to prevent cascading failures

### When to Use Retry
- Transient failures expected
- Operation is idempotent
- User can wait for retry

## Anti-Patterns to Avoid

1. **Distributed Transactions (2PC)**
   - Not used: Poor performance, availability issues
   - Alternative: Saga pattern

2. **Shared Database**
   - Not used: Breaks service autonomy
   - Alternative: Database per service

3. **Synchronous Communication Only**
   - Not used: Tight coupling
   - Alternative: Mix of sync (saga) and async (events)

4. **No Idempotency**
   - Not used: Duplicate processing
   - Alternative: Idempotency keys

5. **No Timeout Handling**
   - Not used: Resource leaks
   - Alternative: Timeout handler

## Future Patterns to Consider

1. **API Gateway Pattern**: Already implemented, enhance with rate limiting
2. **Bulkhead Pattern**: Isolate failures to specific resource pools
3. **Strangler Pattern**: Migrate monolith incrementally
4. **Backend for Frontend (BFF)**: Separate API for each client type
5. **Service Mesh**: Advanced traffic management and security

