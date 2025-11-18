# Code Structure and Organization

## Project Overview

FTGO is a multi-module Maven project implementing a microservices architecture. This document explains the code organization, key classes, and their relationships.

## Module Structure

### Root Module (`ftgo-microservices`)

**Purpose**: Parent POM for all modules

**Key Files**:
- `pom.xml`: Parent POM with dependency management
- `docker-compose.yml`: Infrastructure services
- `README.md`: Project documentation

### Common Module (`ftgo-common`)

**Purpose**: Shared code across all services

**Packages**:
- `com.ftgo.common.domain`: Value objects (Money, Address)
- `com.ftgo.common.events`: Domain events
- `com.ftgo.common.exception`: Custom exceptions

**Key Classes**:
- `Money`: Value object for monetary amounts
- `Address`: Value object for addresses
- `DomainEvent`: Base class for all domain events
- `FTGOException`: Base exception class

**Dependencies**: 
- Spring Boot Web
- Jackson (for JSON)
- Lombok
- Jakarta Persistence API (for @Embeddable)

### Order Service (`ftgo-order-service`)

**Purpose**: Order management and saga orchestration

**Key Packages**:

#### Domain Layer (`domain/`)
- `Order`: Order entity with business logic
- `OrderLineItem`: Order line item entity
- `OrderState`: Order state enumeration
- `OrderRepository`: Repository interface

#### Application Layer (`application/`)
- `OrderService`: Application service for order operations
- `OrderSagaService`: Service for initiating sagas
- `dto/`: Data Transfer Objects
  - `CreateOrderRequest`
  - `OrderDTO`
- `mapper/`: MapStruct mappers
  - `OrderMapper`

#### Saga Package (`saga/`)
- `SagaManager`: Central orchestrator
- `SagaDefinition`: Interface for saga definitions
- `CreateOrderSagaDefinition`: Create Order Saga implementation
- `AsyncSagaStep`: Interface for async saga steps
- `SagaStepRetryHandler`: Retry logic with Resilience4j
- `SemanticLockManager`: Distributed locking with Redis
- `IdempotencyHandler`: Idempotency handling
- `SagaTimeoutHandler`: Timeout detection and handling
- `CreateOrderSagaData`: Saga data transfer object
- `model/`: Saga state models
  - `SagaInstance`: Saga instance entity
  - `SagaStepExecution`: Step execution entity
  - `SagaState`: Saga state enumeration
  - `SagaStepResult`: Step result object
- `steps/`: Saga step implementations
  - `ValidateOrderStep`
  - `CreateTicketStep`
  - `AuthorizeCardStep`
  - `ConfirmCreateOrderStep`

#### Infrastructure Layer (`infrastructure/`)
- `JpaOrderRepository`: JPA repository implementation
- `OrderEventPublisher`: Publishes order events to Kafka
- `OrderEventConsumer`: Consumes restaurant events
- `RestaurantServiceClient`: Client for Restaurant Service

#### Presentation Layer (`presentation/`)
- `OrderController`: REST controller for order operations

#### Configuration (`config/`)
- `RedisConfig`: Redis configuration

### Customer Service (`ftgo-customer-service`)

**Domain**:
- `Customer`: Customer entity
- `PaymentMethod`: Payment method entity

**Application**:
- `CustomerService`: Customer management
- `CustomerMapper`: DTO mapping

**Infrastructure**:
- `JpaCustomerRepository`: JPA implementation
- `CustomerEventPublisher`: Event publishing

**Presentation**:
- `CustomerController`: REST API

### Restaurant Service (`ftgo-restaurant-service`)

**Domain**:
- `Restaurant`: Restaurant entity
- `MenuItem`: Menu item entity

**Application**:
- `RestaurantService`: Restaurant management
- `RestaurantMapper`: DTO mapping

**Infrastructure**:
- `JpaRestaurantRepository`: JPA implementation
- `RestaurantEventPublisher`: Event publishing

**Presentation**:
- `RestaurantController`: REST API

### Kitchen Service (`ftgo-kitchen-service`)

**Domain**:
- `Ticket`: Kitchen ticket entity
- `TicketLineItem`: Ticket line item
- `TicketState`: Ticket state enumeration

**Application**:
- `KitchenService`: Ticket management
- `TicketMapper`: DTO mapping

**Infrastructure**:
- `JpaTicketRepository`: JPA implementation
- `KitchenEventPublisher`: Event publishing
- `KitchenEventConsumer`: Legacy event consumer

**Presentation**:
- `KitchenController`: REST API (called by saga orchestrator)

### Delivery Service (`ftgo-delivery-service`)

**Domain**:
- `Delivery`: Delivery entity
- `Courier`: Courier entity
- `DeliveryState`: Delivery state enumeration

**Application**:
- `DeliveryService`: Delivery management
- `DeliveryMapper`: DTO mapping

**Infrastructure**:
- `JpaDeliveryRepository`: JPA implementation
- `JpaCourierRepository`: JPA implementation
- `DeliveryEventPublisher`: Event publishing
- `DeliveryEventConsumer`: Consumes ticket events

**Presentation**:
- `DeliveryController`: REST API

### Accounting Service (`ftgo-accounting-service`)

**Domain**:
- `Payment`: Payment entity
- `Invoice`: Invoice entity
- `PaymentStatus`: Payment status enumeration

**Application**:
- `AccountingService`: Payment and invoice management
- `AccountingMapper`: DTO mapping

**Infrastructure**:
- `JpaPaymentRepository`: JPA implementation
- `JpaInvoiceRepository`: JPA implementation
- `AccountingEventConsumer`: Legacy event consumer

**Presentation**:
- `AccountingController`: REST API (called by saga orchestrator)

### API Gateway (`ftgo-api-gateway`)

**Purpose**: Single entry point, request routing

**Configuration**:
- `application.yml`: Route definitions
- Routes requests to appropriate services

## Key Design Patterns in Code

### 1. Layered Architecture

Each service follows a layered architecture:

```
Presentation (Controllers)
    ↓
Application (Services, DTOs)
    ↓
Domain (Entities, Business Logic)
    ↓
Infrastructure (JPA, Events, External Clients)
```

### 2. Dependency Injection

- Constructor injection via `@RequiredArgsConstructor`
- Spring's `@Component`, `@Service`, `@Repository` annotations
- Interface-based design for testability

### 3. Repository Pattern

- Interface in domain layer: `OrderRepository`
- Implementation in infrastructure: `JpaOrderRepository`
- Spring Data JPA for query methods

### 4. DTO Pattern

- Separate DTOs for API requests/responses
- MapStruct for entity ↔ DTO mapping
- Prevents exposing internal domain model

### 5. Event-Driven Architecture

- Domain events in entities (transient)
- Event publishers in infrastructure
- Event consumers for side effects
- Kafka for event distribution

## Key Classes and Their Roles

### Saga Orchestration

**SagaManager**:
- Coordinates saga execution
- Manages saga state
- Handles step results and failures
- Triggers compensation

**AsyncSagaStep**:
- Interface for saga steps
- Async execution with callbacks
- Compensation support

**SagaInstance**:
- Represents saga execution
- Persists state to database
- Tracks step executions

### Resilience

**SagaStepRetryHandler**:
- Wraps step execution with retry
- Uses Resilience4j Retry
- Handles exponential backoff

**SemanticLockManager**:
- Distributed locking with Redis
- Prevents concurrent modifications
- Automatic expiration

### Domain Events

**DomainEvent**:
- Base class for all events
- Contains event metadata
- JSON serialization support

**Event Publishers**:
- Publish events to Kafka
- Called after domain operations
- Async publishing

## Code Conventions

### Naming Conventions

- **Entities**: Singular, PascalCase (`Order`, `Customer`)
- **Repositories**: Entity name + `Repository` (`OrderRepository`)
- **Services**: Entity name + `Service` (`OrderService`)
- **Controllers**: Entity name + `Controller` (`OrderController`)
- **DTOs**: Purpose + `DTO` or `Request`/`Response` (`OrderDTO`, `CreateOrderRequest`)
- **Mappers**: Entity name + `Mapper` (`OrderMapper`)

### Package Conventions

- `domain`: Domain entities and repositories
- `application`: Application services, DTOs, mappers
- `infrastructure`: Technical implementations
- `presentation`: REST controllers
- `config`: Configuration classes
- `saga`: Saga-related classes (Order Service only)

### Annotation Usage

- `@Entity`: JPA entities
- `@Table`: Table name mapping
- `@Id`: Primary key
- `@Column`: Column mapping
- `@OneToMany`/`@ManyToOne`: Relationships
- `@Embedded`/`@Embeddable`: Value objects
- `@Service`: Application services
- `@Repository`: Repository implementations
- `@RestController`: REST controllers
- `@Component`: General components
- `@Transactional`: Transaction boundaries
- `@RequiredArgsConstructor`: Constructor injection
- `@Slf4j`: Logging

## Database Schema Organization

### Migration Files

- Location: `src/main/resources/db/migration/`
- Naming: `V{version}__{description}.sql`
- Version: Sequential numbers (V1, V2, V3...)
- Flyway manages migrations automatically

### Table Naming

- Plural, snake_case: `orders`, `order_line_items`
- Foreign keys: `{entity}_id` (e.g., `order_id`)
- Timestamps: `created_at`, `updated_at`

## Event Organization

### Event Naming

- Pattern: `{Entity}{Action}Event`
- Examples: `OrderCreatedEvent`, `TicketReadyEvent`

### Event Structure

```java
public class OrderCreatedEvent extends DomainEvent {
    private String orderId;
    private String customerId;
    // ... other fields
    
    public OrderCreatedEvent(String orderId, ...) {
        super(orderId, "Order");
        // Set fields
    }
}
```

## Testing Structure

### Test Organization

```
src/test/java/com/ftgo/{service}/
├── domain/          # Domain tests
├── application/     # Service tests
└── presentation/    # Controller tests
```

### Test Naming

- Unit tests: `{Class}Test`
- Integration tests: `{Class}IntegrationTest`

## Configuration Files

### application.yml Structure

```yaml
spring:
  application:
    name: {service-name}
  datasource:
    url: jdbc:postgresql://localhost:5432/{database}
    username: ftgo
    password: ftgo123
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
  cloud:
    consul:
      # Service discovery
    stream:
      kafka:
        # Event bindings
resilience4j:
  # Resilience configuration
server:
  port: {port}
management:
  # Actuator configuration
```

## Common Utilities

### Value Objects (ftgo-common)

**Money**:
- Immutable value object
- Currency-aware
- Validation in constructor
- Business methods (add, multiply, compare)

**Address**:
- Immutable value object
- Multiple constructors
- Default country handling

### Exceptions (ftgo-common)

**FTGOException**: Base exception
**EntityNotFoundException**: Resource not found
**InvalidOperationException**: Invalid business operation

## Integration Points

### Service-to-Service Communication

1. **REST (Saga Orchestration)**:
   - SagaManager → Kitchen Service
   - SagaManager → Accounting Service
   - Uses WebClient (reactive)

2. **Events (Notifications)**:
   - Order Service → Kafka → Delivery Service
   - Kitchen Service → Kafka → Delivery Service
   - Uses Spring Cloud Stream

3. **Service Discovery**:
   - All services register with Consul
   - Services discover each other by name
   - Load balancing automatic

## Code Quality

### Dependencies

- **Lombok**: Reduces boilerplate
- **MapStruct**: Type-safe mapping
- **Spring Boot**: Framework
- **Spring Data JPA**: Data access
- **Resilience4j**: Resilience patterns

### Best Practices

1. **Immutability**: Value objects are immutable
2. **Validation**: Input validation at boundaries
3. **Error Handling**: Custom exceptions with clear messages
4. **Logging**: Structured logging with appropriate levels
5. **Transactions**: Explicit transaction boundaries
6. **Idempotency**: Operations are idempotent where possible

## Extension Points

### Adding New Functionality

1. **New Entity**: Add to domain, create repository, add migration
2. **New API**: Add controller, service method, DTOs
3. **New Event**: Create event class, register in DomainEvent, publish
4. **New Saga Step**: Implement AsyncSagaStep, add to saga definition
5. **New Service**: Create module, follow structure, add to root POM

## Code Examples

### Creating a Domain Entity

```java
@Entity
@Table(name = "entities")
@Getter
@NoArgsConstructor
public class Entity {
    @Id
    private String id;
    
    @Column(nullable = false)
    private String name;
    
    @Transient
    private List<DomainEvent> domainEvents = new ArrayList<>();
    
    public Entity(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
    }
    
    public void addDomainEvent(DomainEvent event) {
        domainEvents.add(event);
    }
}
```

### Creating an Application Service

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class EntityService {
    private final EntityRepository repository;
    private final EntityEventPublisher eventPublisher;
    
    @Transactional
    public Entity createEntity(CreateEntityRequest request) {
        Entity entity = new Entity(request.getName());
        entity = repository.save(entity);
        publishDomainEvents(entity);
        return entity;
    }
    
    private void publishDomainEvents(Entity entity) {
        entity.getDomainEvents().forEach(eventPublisher::publish);
        entity.clearDomainEvents();
    }
}
```

### Creating a REST Controller

```java
@RestController
@RequestMapping("/entities")
@RequiredArgsConstructor
@Slf4j
public class EntityController {
    private final EntityService service;
    private final EntityMapper mapper;
    
    @PostMapping
    public ResponseEntity<EntityDTO> create(@Valid @RequestBody CreateEntityRequest request) {
        Entity entity = service.createEntity(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(mapper.toDTO(entity));
    }
}
```

