# FTGO Microservices Application

A complete Food to Go (FTGO) microservices application built with Spring Boot 3.3.x, implementing patterns from "Microservices Patterns: With examples in Java" by Chris Richardson.

## Architecture Overview

This application demonstrates a complete microservices architecture with the following services:

- **Customer Service** - Customer registration and payment method management
- **Restaurant Service** - Restaurant and menu management
- **Order Service** - Order creation and management with Saga pattern
- **Kitchen Service** - Kitchen ticket management
- **Delivery Service** - Courier and delivery tracking
- **Accounting Service** - Billing and payment processing
- **API Gateway** - Single entry point for all client requests

## Technology Stack

- **Java**: 17
- **Spring Boot**: 3.3.4
- **Spring Cloud**: 2023.0.3
- **Database**: PostgreSQL (database per service pattern)
- **Message Broker**: Apache Kafka
- **Service Discovery**: Consul
- **API Gateway**: Spring Cloud Gateway
- **Build Tool**: Maven
- **Containerization**: Docker & Docker Compose
- **Observability**: Spring Cloud Sleuth, Micrometer, Prometheus, Zipkin

## Key Patterns Implemented

1. **Saga Pattern (Choreography-based)** - Distributed transaction management using event-driven communication
2. **CQRS** - Command Query Responsibility Segregation where appropriate
3. **Event Sourcing** - Domain events stored and published via Kafka
4. **API Composition** - API Gateway aggregates requests from multiple services
5. **Service Discovery** - Consul for service registration and discovery
6. **Circuit Breaker** - Resilience4j for fault tolerance
7. **Database per Service** - Each service has its own PostgreSQL database

## Prerequisites

- Java 17 or higher
- Maven 3.8+
- Docker and Docker Compose
- At least 8GB RAM available for Docker containers

## Quick Start

### 1. Start Infrastructure Services

Start all infrastructure services (PostgreSQL, Kafka, Zookeeper, Consul, Zipkin) using Docker Compose:

```bash
docker-compose up -d
```

This will start:
- 6 PostgreSQL databases (one per service)
- Kafka and Zookeeper
- Consul (service discovery)
- Zipkin (distributed tracing)

### 2. Build the Project

Build all modules:

```bash
mvn clean install
```

### 3. Start Microservices

Start each service in separate terminals or use your IDE:

```bash
# Terminal 1 - Customer Service
cd ftgo-customer-service
mvn spring-boot:run

# Terminal 2 - Restaurant Service
cd ftgo-restaurant-service
mvn spring-boot:run

# Terminal 3 - Order Service
cd ftgo-order-service
mvn spring-boot:run

# Terminal 4 - Kitchen Service
cd ftgo-kitchen-service
mvn spring-boot:run

# Terminal 5 - Delivery Service
cd ftgo-delivery-service
mvn spring-boot:run

# Terminal 6 - Accounting Service
cd ftgo-accounting-service
mvn spring-boot:run

# Terminal 7 - API Gateway
cd ftgo-api-gateway
mvn spring-boot:run
```

### 4. Verify Services

- **API Gateway**: http://localhost:8080
- **Customer Service**: http://localhost:8081
- **Restaurant Service**: http://localhost:8082
- **Order Service**: http://localhost:8083
- **Kitchen Service**: http://localhost:8084
- **Delivery Service**: http://localhost:8085
- **Accounting Service**: http://localhost:8086
- **Consul UI**: http://localhost:8500
- **Zipkin**: http://localhost:9411

## API Documentation

Each service exposes Swagger/OpenAPI documentation:

- API Gateway: http://localhost:8080/swagger-ui.html
- Customer Service: http://localhost:8081/swagger-ui.html
- Restaurant Service: http://localhost:8082/swagger-ui.html
- Order Service: http://localhost:8083/swagger-ui.html
- Kitchen Service: http://localhost:8084/swagger-ui.html
- Delivery Service: http://localhost:8085/swagger-ui.html
- Accounting Service: http://localhost:8086/swagger-ui.html

## Example Workflow

### 1. Register a Customer

```bash
curl -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john.doe@example.com",
    "address": {
      "street1": "123 Main St",
      "city": "San Francisco",
      "state": "CA",
      "zip": "94102"
    }
  }'
```

### 2. Create a Restaurant

```bash
curl -X POST http://localhost:8080/api/restaurants \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Pizza Palace",
    "address": "456 Market St, San Francisco, CA 94102"
  }'
```

### 3. Add Menu Items

```bash
curl -X PUT http://localhost:8080/api/restaurants/{restaurantId}/menu \
  -H "Content-Type: application/json" \
  -d '{
    "menuItems": [
      {
        "name": "Margherita Pizza",
        "price": "15.99",
        "currency": "USD"
      },
      {
        "name": "Pepperoni Pizza",
        "price": "17.99",
        "currency": "USD"
      }
    ]
  }'
```

### 4. Create an Order

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "{customerId}",
    "restaurantId": "{restaurantId}",
    "lineItems": [
      {
        "menuItemId": "{menuItemId}",
        "name": "Margherita Pizza",
        "quantity": 2,
        "price": "15.99",
        "currency": "USD"
      }
    ],
    "deliveryAddress": "123 Main St, San Francisco, CA 94102",
    "deliveryTime": "2024-12-20T18:00:00Z"
  }'
```

### 5. Process the Order Flow

1. Order is created and published `OrderCreated` event
2. Kitchen Service creates a ticket when it receives `OrderApproved` event
3. Kitchen marks ticket as ready, publishing `TicketReady` event
4. Delivery Service creates delivery when it receives `TicketReady` event
5. Accounting Service processes payment when delivery is completed

## Project Structure

```
ftgo-microservices/
├── ftgo-common/              # Shared utilities and event definitions
├── ftgo-api-gateway/         # API Gateway service
├── ftgo-customer-service/    # Customer management
├── ftgo-restaurant-service/  # Restaurant and menu management
├── ftgo-order-service/       # Order management
├── ftgo-kitchen-service/     # Kitchen ticket management
├── ftgo-delivery-service/    # Delivery and courier management
├── ftgo-accounting-service/  # Billing and payment processing
├── docker-compose.yml        # Infrastructure services
└── README.md                 # This file
```

## Service Ports

| Service | Port |
|---------|------|
| API Gateway | 8080 |
| Customer Service | 8081 |
| Restaurant Service | 8082 |
| Order Service | 8083 |
| Kitchen Service | 8084 |
| Delivery Service | 8085 |
| Accounting Service | 8086 |

## Database Ports

| Database | Port |
|----------|------|
| Customer DB | 5432 |
| Restaurant DB | 5433 |
| Order DB | 5434 |
| Kitchen DB | 5435 |
| Delivery DB | 5436 |
| Accounting DB | 5437 |

## Event Flow

The application uses event-driven communication via Kafka:

1. **OrderCreated** → Published by Order Service
2. **OrderApproved** → Published by Order Service → Consumed by Kitchen Service
3. **TicketCreated** → Published by Kitchen Service
4. **TicketReady** → Published by Kitchen Service → Consumed by Delivery Service
5. **DeliveryCreated** → Published by Delivery Service
6. **DeliveryDelivered** → Published by Delivery Service → Consumed by Accounting Service

## Observability

### Health Checks

All services expose health endpoints:

```bash
curl http://localhost:8080/actuator/health
```

### Metrics

Prometheus metrics are available at:

```bash
curl http://localhost:8080/actuator/prometheus
```

### Distributed Tracing

Zipkin is available at http://localhost:9411 for distributed tracing across services.

## Testing

Run tests for all services:

```bash
mvn test
```

## Development Guidelines

1. **Code Quality**: Follow SOLID principles and clean architecture
2. **Error Handling**: Use custom exceptions and proper error responses
3. **Logging**: Structured logging with appropriate log levels
4. **Documentation**: API documentation with OpenAPI/Swagger
5. **Versioning**: Semantic versioning for releases

## Troubleshooting

### Services not starting

1. Check if all infrastructure services are running: `docker-compose ps`
2. Verify database connections in service logs
3. Check Consul for service registration: http://localhost:8500

### Kafka connection issues

1. Ensure Zookeeper is running: `docker ps | grep zookeeper`
2. Check Kafka logs: `docker logs ftgo-kafka`
3. Verify Kafka is accessible: `docker exec -it ftgo-kafka kafka-broker-api-versions --bootstrap-server localhost:9092`

### Database migration issues

1. Check Flyway migration logs in service startup
2. Verify database credentials in `application.yml`
3. Ensure databases are created and accessible

## Future Enhancements

- [ ] Add authentication and authorization (JWT/OAuth2)
- [ ] Implement comprehensive integration tests
- [ ] Add contract testing with Spring Cloud Contract
- [ ] Implement caching with Redis
- [ ] Add rate limiting to API Gateway
- [ ] Implement API versioning strategy
- [ ] Add comprehensive monitoring dashboards
- [ ] Implement event sourcing with dedicated event store

## License

This project is for educational purposes, demonstrating microservices patterns.

## References

- "Microservices Patterns: With examples in Java" by Chris Richardson
- Spring Boot Documentation: https://spring.io/projects/spring-boot
- Spring Cloud Documentation: https://spring.io/projects/spring-cloud

