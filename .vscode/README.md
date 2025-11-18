# VS Code Launch Configuration

This directory contains VS Code launch configurations for running the FTGO microservices.

## Launch Configurations

### Individual Services

You can run each service individually:
- **Customer Service** (port 8081)
- **Restaurant Service** (port 8082)
- **Order Service** (port 8083)
- **Kitchen Service** (port 8084)
- **Delivery Service** (port 8085)
- **Accounting Service** (port 8086)
- **API Gateway** (port 8080)
- **Frontend (Dev Server)** (port 5173)

### Launch All Microservices

Use the **"Launch All Microservices"** compound configuration to start all backend services at once.

**How to use:**
1. Press `F5` or go to Run and Debug panel
2. Select **"Launch All Microservices"** from the dropdown
3. Click the play button or press `F5`

**Note:** The compound configuration will automatically:
- Start infrastructure services (PostgreSQL, Kafka, Consul, Redis, Zipkin) via `docker-compose up -d`
- Launch all 7 microservices in parallel
- Stop all services when you stop debugging

### Launch All (Including Frontend)

Use the **"Launch All (Including Frontend)"** compound configuration to start all backend services plus the React frontend.

**How to use:**
1. Press `F5` or go to Run and Debug panel
2. Select **"Launch All (Including Frontend)"** from the dropdown
3. Click the play button or press `F5`

**Note:** This will start all backend services plus the frontend dev server. The frontend will automatically open in your browser when ready.

## Prerequisites

1. **Docker and Docker Compose** must be installed and running
2. **Java 17** must be installed
3. **VS Code Java Extension Pack** must be installed
4. Infrastructure services should be started (done automatically via pre-launch task)

## Tasks

The following tasks are available:

- **start-infrastructure**: Starts all infrastructure services (PostgreSQL, Kafka, Consul, Redis, Zipkin)
- **stop-infrastructure**: Stops all infrastructure services
- **build-all**: Builds all microservices using Maven
- **check-infrastructure**: Checks the status of infrastructure services
- **frontend:dev**: Starts the frontend development server
- **frontend:build**: Builds the frontend for production
- **frontend:install**: Installs frontend dependencies

## Troubleshooting

### Services won't start

1. Ensure Docker is running: `docker ps`
2. Check infrastructure services: Run the "check-infrastructure" task
3. Verify ports are not in use (8080-8086)
4. Check service logs in the Debug Console

### Port conflicts

If you get port conflicts:
- Stop any running instances of the services
- Check if ports 8080-8086 are available: `lsof -i :8080` (macOS/Linux)
- Modify port numbers in `application.yml` if needed

### Database connection errors

1. Ensure PostgreSQL is running: `docker-compose ps`
2. Check database initialization: `docker logs ftgo-postgres`
3. Verify database credentials in `application.yml`

## Manual Start

If you prefer to start services manually:

```bash
# Start infrastructure
docker-compose up -d

# Start each service (in separate terminals)
cd ftgo-customer-service && mvn spring-boot:run
cd ftgo-restaurant-service && mvn spring-boot:run
# ... etc
```

## Service URLs

Once all services are running:

- **Frontend**: http://localhost:5173
- **API Gateway**: http://localhost:8080
- **Customer Service**: http://localhost:8081
- **Restaurant Service**: http://localhost:8082
- **Order Service**: http://localhost:8083
- **Kitchen Service**: http://localhost:8084
- **Delivery Service**: http://localhost:8085
- **Accounting Service**: http://localhost:8086
- **Consul UI**: http://localhost:8500
- **Zipkin**: http://localhost:9411

