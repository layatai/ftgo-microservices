# AI Context Guide for FTGO Microservices

This document provides context for AI assistants to understand the FTGO microservices project structure, patterns, and implementation details.

## Project Summary

FTGO (Food to Go) is a microservices-based food delivery application demonstrating modern distributed system patterns. The application uses Spring Boot 3.3.x and implements orchestration-based sagas for managing distributed transactions.

## Quick Facts

- **7 Microservices**: Customer, Restaurant, Order, Kitchen, Delivery, Accounting, API Gateway
- **Architecture**: Orchestration-based sagas, event-driven communication
- **Database**: Single PostgreSQL instance with separate databases per service (development)
- **Message Broker**: Apache Kafka for event distribution
- **Service Discovery**: Consul
- **Resilience**: Resilience4j (Circuit Breaker, Retry)
- **Distributed Locking**: Redis
- **Observability**: Spring Cloud Sleuth, Zipkin, Prometheus

## Key Implementation Details

### Saga Pattern

- **Type**: Orchestration-based (centralized coordinator)
- **Orchestrator**: `SagaManager` in Order Service
- **Steps**: Async execution with callbacks
- **Retry**: Resilience4j Retry with exponential backoff
- **Compensation**: Automatic compensation on failure
- **State**: Persisted in database (`saga_instances`, `saga_step_executions`)

### Communication Patterns

1. **Orchestration (Saga)**: REST commands from SagaManager to services
2. **Event-Driven**: Kafka events for notifications and eventual consistency
3. **Service Discovery**: Consul for dynamic service location

### Database Strategy

- **Pattern**: Database per Service (logical separation)
- **Implementation**: Single PostgreSQL instance with separate databases
- **Databases**: `ftgo_customer`, `ftgo_restaurant`, `ftgo_order`, `ftgo_kitchen`, `ftgo_delivery`, `ftgo_accounting`
- **Migrations**: Flyway for schema management

### Resilience Features

- **Retry**: Resilience4j Retry (3 attempts, exponential backoff)
- **Circuit Breaker**: Resilience4j Circuit Breaker
- **Timeout Handling**: Automatic saga timeout detection and compensation
- **Semantic Locking**: Redis-based distributed locking

## Code Organization

### Module Structure

```
ftgo-microservices/
├── ftgo-common/              # Shared code (events, value objects, exceptions)
├── ftgo-api-gateway/         # API Gateway (Spring Cloud Gateway)
├── ftgo-customer-service/    # Customer management
├── ftgo-restaurant-service/  # Restaurant and menu management
├── ftgo-order-service/       # Order management + Saga orchestration
├── ftgo-kitchen-service/     # Kitchen ticket management
├── ftgo-delivery-service/    # Delivery tracking
└── ftgo-accounting-service/  # Payment processing
```

### Service Package Structure

Each service follows this structure:
- `domain/`: Entities, repositories (domain layer)
- `application/`: Services, DTOs, mappers (application layer)
- `infrastructure/`: JPA implementations, event publishers/consumers
- `presentation/`: REST controllers
- `config/`: Configuration classes

### Order Service Special Structure

Order Service has additional `saga/` package:
- `SagaManager`: Central orchestrator
- `SagaDefinition`: Saga definition interface
- `AsyncSagaStep`: Step interface
- `SagaStepRetryHandler`: Retry logic
- `SemanticLockManager`: Distributed locking
- `IdempotencyHandler`: Idempotency handling
- `SagaTimeoutHandler`: Timeout management
- `steps/`: Step implementations
- `model/`: Saga state models

## Key Classes Reference

### Saga Components

- **SagaManager** (`ftgo-order-service/saga/SagaManager.java`): Central orchestrator
- **CreateOrderSagaDefinition** (`ftgo-order-service/saga/CreateOrderSagaDefinition.java`): Saga definition
- **AsyncSagaStep** (`ftgo-order-service/saga/AsyncSagaStep.java`): Step interface
- **SagaInstance** (`ftgo-order-service/saga/model/SagaInstance.java`): Saga state entity
- **SagaStepResult** (`ftgo-order-service/saga/model/SagaStepResult.java`): Step result object

### Saga Steps

- **ValidateOrderStep**: Validates order (Order Service, local)
- **CreateTicketStep**: Creates ticket (Kitchen Service, REST)
- **AuthorizeCardStep**: Authorizes payment (Accounting Service, REST)
- **ConfirmCreateOrderStep**: Confirms order (Order Service, local)

### Resilience Components

- **SagaStepRetryHandler**: Wraps steps with Resilience4j Retry
- **SemanticLockManager**: Redis-based distributed locking
- **IdempotencyHandler**: Prevents duplicate saga creation

### Domain Events

All events extend `DomainEvent` (`ftgo-common/events/DomainEvent.java`):
- Customer: `CustomerCreatedEvent`, `PaymentMethodAddedEvent`
- Restaurant: `RestaurantCreatedEvent`, `MenuUpdatedEvent`
- Order: `OrderCreatedEvent`, `OrderApprovedEvent`, `OrderRejectedEvent`, `OrderCancelledEvent`
- Kitchen: `TicketCreatedEvent`, `TicketAcceptedEvent`, `TicketPreparingEvent`, `TicketReadyEvent`
- Delivery: `DeliveryCreatedEvent`, `DeliveryPickedUpEvent`, `DeliveryDeliveredEvent`

## Configuration Locations

### Application Configuration

- **Order Service**: `ftgo-order-service/src/main/resources/application.yml`
  - Saga retry configuration
  - Redis configuration
  - Circuit breaker configuration

### Infrastructure Configuration

- **Docker Compose**: `docker-compose.yml`
- **Database Init**: `docker/init-multiple-databases.sh`

## Common Operations

### Creating an Order (Saga Flow)

1. Client → `POST /api/orders` with `Idempotency-Key` header
2. `OrderController.createOrder()` → `OrderService.createOrder()`
3. `OrderService` → `OrderSagaService.createOrderSaga()`
4. `OrderSagaService` → Idempotency check, acquire lock
5. `OrderSagaService` → `SagaManager.createSagaInstance()`
6. `SagaManager` → `executeNextStep()` immediately
7. `SagaManager` → `SagaStepRetryHandler.executeWithRetry()`
8. Step executes → Reports result via callback
9. `SagaManager` → Handles result, executes next step
10. Saga completes → All steps succeeded

### Saga Failure Flow

1. Step fails → Retry (if retryable) up to 3 attempts
2. Retries exhausted → `handleStepFailure()` called
3. `SagaManager` → `compensateSaga()`
4. Compensation executes in reverse order
5. Saga state → COMPENSATED

## Important Patterns

### 1. Async Callback Pattern

Saga steps use async callbacks:
```java
step.executeAsync(sagaData, result -> {
    if (result.isSuccess()) {
        handleStepResult(...);
    } else {
        handleStepFailure(...);
    }
});
```

### 2. Retry Wrapper Pattern

Retry wraps async execution:
```java
CompletableFuture<SagaStepResult> future = new CompletableFuture<>();
step.executeAsync(sagaData, result -> future.complete(result));
Retry retry = retryRegistry.retry("saga-step-retry");
return retry.executeCompletionStage(() -> future);
```

### 3. Event Publishing Pattern

Domain events published after state changes:
```java
entity.addDomainEvent(new EntityCreatedEvent(...));
// ... save entity ...
publishDomainEvents(entity);
```

## Technology Versions

- Java: 17
- Spring Boot: 3.3.4
- Spring Cloud: 2023.0.3
- PostgreSQL: 16-alpine
- Kafka: 7.5.0
- Consul: 1.17
- Redis: 7-alpine
- Resilience4j: Latest (via Spring Cloud)

## Common File Locations

### Configuration Files
- `application.yml`: Service configuration (each service)
- `docker-compose.yml`: Infrastructure services
- `pom.xml`: Maven dependencies (root and each service)

### Database Migrations
- Location: `{service}/src/main/resources/db/migration/`
- Naming: `V{version}__{description}.sql`

### Main Application Classes
- `{Service}Application.java`: Spring Boot main class (each service)

## Testing Considerations

- Unit tests: Domain logic, services
- Integration tests: REST endpoints, database
- Saga tests: Complete saga flows, failures, compensation
- Retry tests: Verify retry behavior
- Idempotency tests: Duplicate request handling

## Common Modifications

### Adding a New Saga Step

1. Create class implementing `AsyncSagaStep`
2. Add to `CreateOrderSagaDefinition.getSteps()`
3. Update `SagaManager.storeStepResult()` if step returns data
4. Test step execution and compensation

### Adding a New Service

1. Create module structure
2. Add to root POM
3. Create domain entities
4. Create application service
5. Create REST controller
6. Create database migration
7. Configure `application.yml`

### Adding a New Domain Event

1. Create event class extending `DomainEvent`
2. Register in `DomainEvent` `@JsonSubTypes`
3. Publish event in service
4. Create consumer if needed

## Key Design Decisions

1. **Orchestration over Choreography**: Better visibility and debugging
2. **Database per Service**: True isolation, independent scaling
3. **Resilience4j**: Standard library for resilience
4. **Redis for Locking**: Distributed locking without DB locks
5. **Single PostgreSQL (Dev)**: Simplified development
6. **Async Callbacks**: Non-blocking saga execution
7. **Event-Driven (Secondary)**: Loose coupling for notifications

## Documentation Files

- `README.md`: Project overview and quick start
- `ARCHITECTURE.md`: System architecture details
- `SAGA_IMPLEMENTATION.md`: Saga pattern deep dive
- `API_DOCUMENTATION.md`: Complete API reference
- `DEVELOPMENT_GUIDE.md`: Development workflows
- `PATTERNS.md`: Pattern reference guide
- `CODE_STRUCTURE.md`: Code organization details
- `AI_CONTEXT.md`: This file (AI assistant context)

## When Modifying Code

### Before Making Changes

1. Understand the pattern being used
2. Check existing similar implementations
3. Review documentation files
4. Consider impact on other services

### Common Pitfalls

1. **Don't** use distributed transactions (2PC)
2. **Don't** share databases between services
3. **Don't** make saga steps synchronous
4. **Don't** forget idempotency
5. **Don't** skip compensation implementation
6. **Don't** ignore timeout handling

### Best Practices

1. Keep steps focused and atomic
2. Make operations idempotent
3. Handle failures gracefully
4. Log important events
5. Test failure scenarios
6. Use appropriate transaction boundaries

## Quick Reference

### Service Ports
- API Gateway: 8080
- Customer: 8081
- Restaurant: 8082
- Order: 8083
- Kitchen: 8084
- Delivery: 8085
- Accounting: 8086

### Database Connection
- Host: localhost
- Port: 5432
- User: ftgo
- Password: ftgo123

### Infrastructure Ports
- PostgreSQL: 5432
- Kafka: 9092
- Zookeeper: 2181
- Consul: 8500
- Zipkin: 9411
- Redis: 6379

This context should help AI assistants understand the project structure and make appropriate suggestions or modifications.

