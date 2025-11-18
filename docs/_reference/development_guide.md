# Development Guide

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.8+
- Docker and Docker Compose
- IDE (IntelliJ IDEA, Eclipse, VS Code)
- At least 8GB RAM for Docker containers

### Initial Setup

1. **Clone the repository** (if applicable)
   ```bash
   git clone <repository-url>
   cd ftgo-microservices
   ```

2. **Start Infrastructure**
   ```bash
   docker-compose up -d
   ```
   This starts:
   - PostgreSQL (port 5432)
   - Kafka + Zookeeper (ports 9092, 2181)
   - Consul (port 8500)
   - Zipkin (port 9411)
   - Redis (port 6379)

3. **Verify Infrastructure**
   ```bash
   docker-compose ps
   ```
   All services should show as "healthy"

4. **Build Project**
   ```bash
   mvn clean install
   ```

5. **Start Services**
   Start each service in separate terminals or use your IDE's run configuration.

## Project Structure

```
ftgo-microservices/
├── pom.xml                          # Root POM
├── docker-compose.yml               # Infrastructure services
├── docker/
│   └── init-multiple-databases.sh  # Database initialization
├── ftgo-common/                     # Shared code
│   └── src/main/java/com/ftgo/common/
│       ├── domain/                  # Shared value objects (Money, Address)
│       ├── events/                  # Domain events
│       └── exception/               # Custom exceptions
├── ftgo-api-gateway/                # API Gateway
├── ftgo-customer-service/           # Customer Service
├── ftgo-restaurant-service/         # Restaurant Service
├── ftgo-order-service/              # Order Service (with Saga)
├── ftgo-kitchen-service/            # Kitchen Service
├── ftgo-delivery-service/           # Delivery Service
└── ftgo-accounting-service/         # Accounting Service
```

## Service Structure

Each service follows this structure:

```
{service}/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/ftgo/{service}/
│   │   │   ├── {Service}Application.java
│   │   │   ├── domain/              # Domain layer
│   │   │   │   ├── {Entity}.java
│   │   │   │   └── {Entity}Repository.java
│   │   │   ├── application/         # Application layer
│   │   │   │   ├── {Service}.java
│   │   │   │   ├── dto/
│   │   │   │   └── mapper/
│   │   │   ├── infrastructure/       # Infrastructure layer
│   │   │   │   ├── Jpa{Entity}Repository.java
│   │   │   │   ├── {Service}EventPublisher.java
│   │   │   │   └── {Service}EventConsumer.java
│   │   │   └── presentation/        # Presentation layer
│   │   │       └── {Service}Controller.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/        # Flyway migrations
│   └── test/
└── target/
```

## Development Workflow

### Adding a New Service

1. **Create Module Structure**
   ```bash
   mkdir -p ftgo-{service}-service/src/main/java/com/ftgo/{service}/
   mkdir -p ftgo-{service}-service/src/main/resources/db/migration
   ```

2. **Add to Root POM**
   - Add module to `<modules>` section

3. **Create Service POM**
   - Copy from existing service
   - Update artifact ID and dependencies

4. **Create Application Class**
   ```java
   @SpringBootApplication
   @EnableDiscoveryClient
   public class {Service}Application {
       public static void main(String[] args) {
           SpringApplication.run({Service}Application.class, args);
       }
   }
   ```

5. **Create Domain Entities**
   - JPA entities
   - Repository interfaces

6. **Create Application Service**
   - Business logic
   - DTOs and mappers

7. **Create REST Controller**
   - API endpoints
   - Request/response DTOs

8. **Create Database Migration**
   - Flyway migration script

9. **Configure application.yml**
   - Database connection
   - Service discovery
   - Kafka bindings

### Adding a New Saga Step

1. **Create Step Class**
   ```java
   @Component
   public class NewStep implements AsyncSagaStep {
       @Override
       public String getName() {
           return "NewStep";
       }
       
       @Override
       public void executeAsync(Object sagaData, Consumer<SagaStepResult> callback) {
           // Implementation
       }
       
       @Override
       public boolean hasCompensation() {
           return true;
       }
       
       @Override
       public void compensateAsync(Object sagaData, Consumer<SagaStepResult> callback) {
           // Compensation implementation
       }
   }
   ```

2. **Add to Saga Definition**
   ```java
   // In CreateOrderSagaDefinition
   private final NewStep newStep;
   
   @Override
   public List<AsyncSagaStep> getSteps() {
       return Arrays.asList(
           validateOrderStep,
           createTicketStep,
           newStep,  // Add here
           authorizeCardStep,
           confirmCreateOrderStep
       );
   }
   ```

3. **Update Saga Data**
   - Add fields to `CreateOrderSagaData` if step returns data
   - Update `SagaManager.storeStepResult()` if needed

### Adding a New Domain Event

1. **Create Event Class**
   ```java
   @Getter
   @Setter
   public class NewEvent extends DomainEvent {
       private String entityId;
       // Other fields
       
       public NewEvent(String entityId, ...) {
           super(entityId, "EntityType");
           this.entityId = entityId;
           // Set other fields
       }
   }
   ```

2. **Register in DomainEvent**
   ```java
   // In DomainEvent.java
   @JsonSubTypes.Type(value = NewEvent.class, name = "New")
   ```

3. **Publish Event**
   ```java
   // In service
   entity.addDomainEvent(new NewEvent(entityId, ...));
   publishDomainEvents(entity);
   ```

4. **Consume Event** (if needed)
   ```java
   @Component
   public class NewEventConsumer {
       @EventListener
       public void handle(NewEvent event) {
           // Handle event
       }
   }
   ```

## Code Style and Conventions

### Package Naming

- Domain: `com.ftgo.{service}.domain`
- Application: `com.ftgo.{service}.application`
- Infrastructure: `com.ftgo.{service}.infrastructure`
- Presentation: `com.ftgo.{service}.presentation`

### Class Naming

- Entities: `Customer`, `Order`, `Ticket`
- Repositories: `CustomerRepository`, `OrderRepository`
- Services: `CustomerService`, `OrderService`
- Controllers: `CustomerController`, `OrderController`
- DTOs: `CustomerDTO`, `CreateOrderRequest`
- Mappers: `CustomerMapper`, `OrderMapper`

### Method Naming

- Query methods: `get*`, `find*`, `list*`
- Command methods: `create*`, `update*`, `delete*`, `cancel*`
- Saga methods: `create*Saga`, `execute*`, `compensate*`

### Annotations

- Use `@RequiredArgsConstructor` for dependency injection
- Use `@Slf4j` for logging
- Use `@Transactional` for write operations
- Use `@Transactional(readOnly = true)` for read operations
- Use `@Valid` for request validation

## Testing

### Unit Tests

- Test domain logic
- Test application services
- Mock external dependencies

### Integration Tests

- Test REST endpoints
- Test database operations
- Test event publishing/consumption

### Saga Tests

- Test successful saga completion
- Test step failures
- Test compensation
- Test idempotency
- Test retry logic

## Database Migrations

### Creating Migrations

1. Create file: `V{version}__{description}.sql`
2. Use Flyway naming convention
3. Place in `src/main/resources/db/migration/`

### Migration Best Practices

- Make migrations idempotent where possible
- Test migrations on clean database
- Never modify existing migrations
- Use transactions for data migrations

## Configuration

### Environment-Specific Configuration

Create profiles for different environments:

```yaml
# application-dev.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ftgo_order

# application-prod.yml
spring:
  datasource:
    url: jdbc:postgresql://prod-db:5432/ftgo_order
```

### External Configuration

Use environment variables or config server:

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/ftgo_order}
    username: ${DB_USER:ftgo}
    password: ${DB_PASSWORD:ftgo123}
```

## Debugging

### Logging

- Use structured logging
- Include correlation IDs
- Log at appropriate levels:
  - `DEBUG`: Detailed information for debugging
  - `INFO`: General information
  - `WARN`: Warning messages
  - `ERROR`: Error messages

### Distributed Tracing

- View traces in Zipkin: http://localhost:9411
- Trace ID is automatically propagated
- Search by service name or trace ID

### Saga Debugging

1. Check saga state in database:
   ```sql
   SELECT * FROM saga_instances WHERE id = 'saga-id';
   SELECT * FROM saga_step_executions WHERE saga_instance_id = 'saga-id';
   ```

2. Check Redis locks:
   ```bash
   redis-cli
   KEYS saga:lock:*
   GET saga:lock:Order:order-id
   ```

3. Check logs for:
   - Saga creation
   - Step execution
   - Retry attempts
   - Compensation execution

## Common Issues and Solutions

### Service Won't Start

1. Check database connection
2. Verify service discovery (Consul)
3. Check port conflicts
4. Review application logs

### Saga Stuck

1. Check saga state in database
2. Verify step execution logs
3. Check for timeout (use timeout handler)
4. Verify callback is being called

### Database Migration Fails

1. Check Flyway logs
2. Verify database exists
3. Check migration file syntax
4. Ensure no conflicting migrations

### Event Not Received

1. Verify Kafka is running
2. Check topic name
3. Verify consumer group
4. Check event serialization

## Performance Tips

1. **Connection Pooling**: Configure HikariCP properly
2. **Lazy Loading**: Use `@OneToMany(fetch = FetchType.LAZY)`
3. **Batch Operations**: Use batch inserts/updates
4. **Caching**: Consider Redis for read-heavy operations
5. **Async Processing**: Use async for non-critical operations

## Security Considerations

### Current State

- No authentication/authorization
- No input validation beyond basic
- No rate limiting
- No API versioning

### Production Recommendations

1. Add JWT/OAuth2 authentication
2. Implement role-based access control
3. Add input validation and sanitization
4. Implement rate limiting
5. Use HTTPS
6. Add API versioning
7. Implement audit logging
8. Add secrets management

## Git Workflow

### Branching Strategy

- `main`: Production-ready code
- `develop`: Integration branch
- `feature/*`: Feature branches
- `fix/*`: Bug fix branches

### Commit Messages

Use conventional commits:
- `feat: Add new saga step`
- `fix: Fix retry logic`
- `docs: Update API documentation`
- `refactor: Simplify saga manager`

## IDE Setup

### IntelliJ IDEA

1. Import as Maven project
2. Enable annotation processing (for Lombok, MapStruct)
3. Install Lombok plugin
4. Configure code style
5. Set up run configurations for each service

### VS Code

1. Install Java Extension Pack
2. Install Spring Boot Extension Pack
3. Configure launch.json for services
4. Install Lombok extension

## Resources

- Spring Boot Documentation: https://spring.io/projects/spring-boot
- Spring Cloud Documentation: https://spring.io/projects/spring-cloud
- Resilience4j Documentation: https://resilience4j.readme.io/
- Kafka Documentation: https://kafka.apache.org/documentation/

