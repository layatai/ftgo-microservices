# FTGO Microservices Architecture

## Overview

FTGO (Food to Go) is a microservices-based food delivery application that demonstrates modern distributed system patterns. The application is built using Spring Boot 3.3.x and implements orchestration-based sagas for managing distributed transactions.

## System Architecture

### Service Decomposition

The application is decomposed into 7 microservices, each with a specific business capability:

1. **Customer Service** (`ftgo-customer-service`)
   - **Responsibility**: Customer registration, profile management, payment method management
   - **Port**: 8081
   - **Database**: `ftgo_customer`
   - **Key Entities**: Customer, PaymentMethod
   - **Events Published**: CustomerCreatedEvent, PaymentMethodAddedEvent

2. **Restaurant Service** (`ftgo-restaurant-service`)
   - **Responsibility**: Restaurant registration, menu management
   - **Port**: 8082
   - **Database**: `ftgo_restaurant`
   - **Key Entities**: Restaurant, MenuItem
   - **Events Published**: RestaurantCreatedEvent, MenuUpdatedEvent

3. **Order Service** (`ftgo-order-service`)
   - **Responsibility**: Order creation, order lifecycle management, saga orchestration
   - **Port**: 8083
   - **Database**: `ftgo_order`
   - **Key Entities**: Order, OrderLineItem
   - **Special Features**: Saga orchestrator (SagaManager), distributed locking (Redis)
   - **Events Published**: OrderCreatedEvent, OrderApprovedEvent, OrderRejectedEvent, OrderCancelledEvent

4. **Kitchen Service** (`ftgo-kitchen-service`)
   - **Responsibility**: Kitchen ticket management, order preparation tracking
   - **Port**: 8084
   - **Database**: `ftgo_kitchen`
   - **Key Entities**: Ticket, TicketLineItem
   - **Events Published**: TicketCreatedEvent, TicketAcceptedEvent, TicketPreparingEvent, TicketReadyEvent

5. **Delivery Service** (`ftgo-delivery-service`)
   - **Responsibility**: Delivery management, courier assignment and tracking
   - **Port**: 8085
   - **Database**: `ftgo_delivery`
   - **Key Entities**: Delivery, Courier
   - **Events Published**: DeliveryCreatedEvent, DeliveryPickedUpEvent, DeliveryDeliveredEvent

6. **Accounting Service** (`ftgo-accounting-service`)
   - **Responsibility**: Payment processing, invoice management
   - **Port**: 8086
   - **Database**: `ftgo_accounting`
   - **Key Entities**: Payment, Invoice
   - **Events Consumed**: DeliveryDeliveredEvent (for payment processing)

7. **API Gateway** (`ftgo-api-gateway`)
   - **Responsibility**: Single entry point, request routing, API composition
   - **Port**: 8080
   - **Technology**: Spring Cloud Gateway
   - **Features**: Service discovery integration, request routing

## Communication Patterns

### 1. Orchestration-Based Saga (Primary Pattern)

**Used For**: Order creation flow (distributed transaction)

**How It Works**:
- `SagaManager` in Order Service orchestrates the entire flow
- Steps execute sequentially via REST commands
- Each step reports back via async callbacks
- On failure, compensating transactions execute in reverse order

**Flow**:
```
OrderService → SagaManager → ValidateOrderStep
                          → CreateTicketStep (REST to Kitchen Service)
                          → AuthorizeCardStep (REST to Accounting Service)
                          → ConfirmCreateOrderStep
```

### 2. Event-Driven Communication (Secondary Pattern)

**Used For**: Notifications and eventual consistency

**How It Works**:
- Services publish domain events to Kafka
- Other services consume events for side effects
- Events are used for notifications, not for saga coordination

**Event Flow**:
- OrderCreated → Published when order is created
- OrderApproved → Published after saga validation
- TicketReady → Consumed by Delivery Service
- DeliveryDelivered → Consumed by Accounting Service

### 3. Synchronous REST (For Saga Steps)

**Used For**: Saga step execution

**How It Works**:
- SagaManager makes REST calls to other services
- Services respond synchronously
- Results are reported back via callbacks

## Data Management

### Database Strategy

**Pattern**: Database per Service (logical separation)

**Implementation**:
- Single PostgreSQL instance (development)
- Separate databases for each service
- Each service owns its data completely
- No shared databases

**Databases**:
- `ftgo_customer` - Customer Service
- `ftgo_restaurant` - Restaurant Service
- `ftgo_order` - Order Service (includes saga tables)
- `ftgo_kitchen` - Kitchen Service
- `ftgo_delivery` - Delivery Service
- `ftgo_accounting` - Accounting Service

### Data Consistency

**Saga Pattern**: Ensures eventual consistency across services
- Compensating transactions handle failures
- No distributed transactions (2PC)
- Each service maintains its own consistency

## Infrastructure Components

### Service Discovery

**Technology**: Consul
- Services register on startup
- Health checks every 10 seconds
- Service-to-service communication uses service names

### Message Broker

**Technology**: Apache Kafka
- Used for event publishing
- Topics: `order-events`, `restaurant-events`, etc.
- Spring Cloud Stream for integration

### Distributed Locking

**Technology**: Redis
- Used for semantic locking in sagas
- Prevents concurrent modifications
- Lock expiration: 30 minutes (matches saga timeout)

### API Gateway

**Technology**: Spring Cloud Gateway
- Routes requests to appropriate services
- Load balancing via service discovery
- Single entry point for clients

## Resilience Patterns

### 1. Circuit Breaker

**Technology**: Resilience4j
- Applied to external service calls
- Prevents cascading failures
- Configurable failure thresholds

### 2. Retry with Exponential Backoff

**Technology**: Resilience4j Retry
- Applied to saga step execution
- Exponential backoff: 1s, 2s, 4s, 8s (capped at 30s)
- Max retries: 3 attempts
- Retries only transient failures (network errors, 5xx)

### 3. Saga Timeout Handling

**Implementation**: Scheduled task in Order Service
- Checks for timed-out sagas every minute
- Saga timeout: 30 minutes
- Step timeout: 5 minutes
- Automatically triggers compensation

## Observability

### Distributed Tracing

**Technology**: Spring Cloud Sleuth + Zipkin
- Trace requests across services
- View complete request flow
- Available at http://localhost:9411

### Metrics

**Technology**: Micrometer + Prometheus
- Exposed at `/actuator/prometheus`
- Saga metrics, retry metrics, circuit breaker metrics
- Integration with Prometheus for monitoring

### Health Checks

**Technology**: Spring Boot Actuator
- Health endpoint: `/actuator/health`
- Service discovery integration
- Custom health indicators for circuit breakers

## Security Considerations

**Current State**: No authentication/authorization implemented
**Future**: JWT/OAuth2 recommended for production

## Scalability

### Horizontal Scaling

- Services are stateless (except database)
- Can scale each service independently
- Service discovery handles load balancing
- Database per service allows independent scaling

### Performance Optimizations

- Connection pooling (HikariCP)
- Redis connection pooling
- Async saga step execution
- Event-driven for non-critical paths

## Deployment Architecture

### Development Environment

- Docker Compose for infrastructure
- Services run as separate processes
- Single PostgreSQL instance (all databases)
- Local Kafka, Consul, Redis, Zipkin

### Production Considerations

- Each service should be containerized (Docker)
- Kubernetes recommended for orchestration
- Separate PostgreSQL instances per service
- Managed Kafka cluster
- Redis cluster for high availability
- Service mesh (Istio/Linkerd) for advanced features

## Key Design Decisions

1. **Orchestration over Choreography**: Centralized control for better visibility and debugging
2. **Database per Service**: True isolation, independent scaling
3. **Event Sourcing**: Domain events for audit and eventual consistency
4. **Resilience4j**: Standard library for resilience patterns
5. **Redis for Locking**: Distributed locking without database locks
6. **Single PostgreSQL (Dev)**: Simplified development setup
7. **Spring Cloud**: Standard Spring ecosystem for microservices

## Code Organization

### Package Structure (Per Service)

```
com.ftgo.{service}/
├── {Service}Application.java      # Main Spring Boot application
├── domain/                        # Domain entities and repositories
│   ├── {Entity}.java
│   └── {Entity}Repository.java
├── application/                   # Application services and DTOs
│   ├── {Service}.java            # Application service
│   ├── dto/                       # Data Transfer Objects
│   └── mapper/                    # MapStruct mappers
├── infrastructure/                # Infrastructure implementations
│   ├── Jpa{Entity}Repository.java
│   ├── {Service}EventPublisher.java
│   └── {Service}EventConsumer.java
└── presentation/                  # REST controllers
    └── {Service}Controller.java
```

### Order Service Special Structure

```
com.ftgo.orderservice/
├── saga/                          # Saga orchestration components
│   ├── SagaManager.java          # Central orchestrator
│   ├── SagaDefinition.java       # Saga definition interface
│   ├── CreateOrderSagaDefinition.java
│   ├── AsyncSagaStep.java        # Step interface
│   ├── SagaStepRetryHandler.java # Retry logic
│   ├── SemanticLockManager.java  # Distributed locking
│   ├── IdempotencyHandler.java   # Idempotency handling
│   ├── SagaTimeoutHandler.java   # Timeout management
│   ├── model/                     # Saga state models
│   └── steps/                     # Saga step implementations
└── ...
```

## Technology Versions

- **Java**: 17
- **Spring Boot**: 3.3.4
- **Spring Cloud**: 2023.0.3
- **PostgreSQL**: 16-alpine
- **Kafka**: 7.5.0
- **Consul**: 1.17
- **Redis**: 7-alpine
- **Resilience4j**: Latest (via Spring Cloud)

## Future Enhancements

1. Authentication/Authorization (JWT/OAuth2)
2. API versioning strategy
3. Rate limiting at API Gateway
4. Caching layer (Redis for reads)
5. Event sourcing with dedicated event store
6. Saga visualization dashboard
7. Comprehensive integration tests
8. Contract testing (Spring Cloud Contract)
