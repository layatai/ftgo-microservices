# API Documentation

## Overview

This document describes all REST API endpoints exposed by the FTGO microservices. All services use Spring Boot and expose Swagger/OpenAPI documentation at `/swagger-ui.html`.

## Base URLs

- **API Gateway**: http://localhost:8080/api
- **Customer Service**: http://localhost:8081/api
- **Restaurant Service**: http://localhost:8082/api
- **Order Service**: http://localhost:8083/api
- **Kitchen Service**: http://localhost:8084/api
- **Delivery Service**: http://localhost:8085/api
- **Accounting Service**: http://localhost:8086/api

## Common Headers

### Idempotency-Key (Optional)

Used for idempotent operations, especially order creation.

```
Idempotency-Key: unique-request-id-123
```

## Customer Service API

### Create Customer

**Endpoint**: `POST /api/customers`

**Request Body**:
```json
{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "address": {
    "street1": "123 Main St",
    "street2": "Apt 4B",
    "city": "San Francisco",
    "state": "CA",
    "zip": "94102",
    "country": "USA"
  }
}
```

**Response**: `201 Created`
```json
{
  "id": "customer-id-123",
  "name": "John Doe",
  "email": "john.doe@example.com",
  "address": { ... }
}
```

### Get Customer

**Endpoint**: `GET /api/customers/{customerId}`

**Response**: `200 OK`
```json
{
  "id": "customer-id-123",
  "name": "John Doe",
  "email": "john.doe@example.com",
  "address": { ... },
  "paymentMethods": [ ... ]
}
```

### Add Payment Method

**Endpoint**: `POST /api/customers/{customerId}/payment-methods`

**Request Body**:
```json
{
  "cardNumber": "4111111111111111",
  "expiryMonth": 12,
  "expiryYear": 2025,
  "cvv": "123"
}
```

**Response**: `201 Created`

## Restaurant Service API

### Create Restaurant

**Endpoint**: `POST /api/restaurants`

**Request Body**:
```json
{
  "name": "Pizza Palace",
  "address": "456 Market St, San Francisco, CA 94102"
}
```

**Response**: `201 Created`
```json
{
  "id": "restaurant-id-123",
  "name": "Pizza Palace",
  "address": "456 Market St, San Francisco, CA 94102"
}
```

### Get Restaurant

**Endpoint**: `GET /api/restaurants/{restaurantId}`

**Response**: `200 OK`

### Update Menu

**Endpoint**: `PUT /api/restaurants/{restaurantId}/menu`

**Request Body**:
```json
{
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
}
```

**Response**: `200 OK`

## Order Service API

### Create Order

**Endpoint**: `POST /api/orders`

**Headers**:
- `Idempotency-Key` (optional): Prevents duplicate order creation

**Request Body**:
```json
{
  "customerId": "customer-id-123",
  "restaurantId": "restaurant-id-456",
  "lineItems": [
    {
      "menuItemId": "menu-item-789",
      "name": "Margherita Pizza",
      "quantity": 2,
      "price": "15.99",
      "currency": "USD"
    }
  ],
  "deliveryAddress": "123 Main St, San Francisco, CA 94102",
  "deliveryTime": "2024-12-20T18:00:00Z"
}
```

**Response**: `201 Created`
```json
{
  "id": "order-id-123",
  "customerId": "customer-id-123",
  "restaurantId": "restaurant-id-456",
  "state": "APPROVED",
  "orderTotal": {
    "amount": 31.98,
    "currency": "USD"
  },
  "lineItems": [ ... ],
  "deliveryAddress": "123 Main St, San Francisco, CA 94102",
  "deliveryTime": "2024-12-20T18:00:00Z",
  "createdAt": "2024-12-20T17:00:00Z"
}
```

**Saga Flow**:
1. Order created in database
2. Create Order Saga initiated
3. Saga orchestrates: Validate → Create Ticket → Authorize Payment → Confirm
4. Returns order immediately (saga continues asynchronously)

### Get Order

**Endpoint**: `GET /api/orders/{orderId}`

**Response**: `200 OK`
```json
{
  "id": "order-id-123",
  "customerId": "customer-id-123",
  "restaurantId": "restaurant-id-456",
  "state": "APPROVED",
  "orderTotal": { ... },
  "lineItems": [ ... ],
  ...
}
```

### Cancel Order

**Endpoint**: `PUT /api/orders/{orderId}/cancel?reason=Customer%20requested`

**Response**: `200 OK`

## Kitchen Service API

### Create Ticket

**Endpoint**: `POST /api/tickets`

**Request Body**:
```json
{
  "orderId": "order-id-123",
  "restaurantId": "restaurant-id-456",
  "lineItems": [
    {
      "menuItemId": "menu-item-789",
      "name": "Margherita Pizza",
      "quantity": 2
    }
  ]
}
```

**Response**: `201 Created`
```
ticket-id-123
```

**Note**: This endpoint is called by the saga orchestrator, not directly by clients.

### Get Ticket

**Endpoint**: `GET /api/tickets/{ticketId}`

**Response**: `200 OK`

### Cancel Ticket

**Endpoint**: `DELETE /api/tickets/{ticketId}`

**Response**: `200 OK`

**Note**: Used for saga compensation.

### Update Ticket State

**Endpoint**: `PUT /api/tickets/{ticketId}/accept`
**Endpoint**: `PUT /api/tickets/{ticketId}/preparing`
**Endpoint**: `PUT /api/tickets/{ticketId}/ready`

**Response**: `200 OK`

## Delivery Service API

### Create Delivery

**Endpoint**: `POST /api/deliveries`

**Request Body**:
```json
{
  "orderId": "order-id-123",
  "deliveryAddress": "123 Main St, San Francisco, CA 94102"
}
```

**Response**: `201 Created`

**Note**: Typically created via event consumption (TicketReadyEvent).

### Get Delivery

**Endpoint**: `GET /api/deliveries/{deliveryId}`

**Response**: `200 OK`

### Update Delivery State

**Endpoint**: `PUT /api/deliveries/{deliveryId}/pickup`
**Endpoint**: `PUT /api/deliveries/{deliveryId}/deliver`

**Response**: `200 OK`

## Accounting Service API

### Authorize Payment

**Endpoint**: `POST /api/payments/authorize`

**Request Body**:
```json
{
  "customerId": "customer-id-123",
  "orderId": "order-id-123",
  "amount": {
    "amount": 31.98,
    "currency": "USD"
  }
}
```

**Response**: `200 OK`
```
payment-id-123
```

**Note**: This endpoint is called by the saga orchestrator.

### Release Authorization

**Endpoint**: `DELETE /api/payments/{paymentId}/authorization`

**Response**: `200 OK`

**Note**: Used for saga compensation.

### Get Payment

**Endpoint**: `GET /api/payments/{paymentId}`

**Response**: `200 OK`

## Error Responses

### 400 Bad Request

```json
{
  "message": "Validation error message"
}
```

### 404 Not Found

```json
{
  "message": "Entity not found with id: {id}"
}
```

### 500 Internal Server Error

```json
{
  "message": "Internal server error"
}
```

## API Gateway Routes

The API Gateway routes requests to appropriate services:

- `/api/customers/**` → Customer Service (8081)
- `/api/restaurants/**` → Restaurant Service (8082)
- `/api/orders/**` → Order Service (8083)
- `/api/tickets/**` → Kitchen Service (8084)
- `/api/deliveries/**` → Delivery Service (8085)
- `/api/payments/**` → Accounting Service (8086)

## Authentication

**Current State**: No authentication implemented

**Future**: JWT/OAuth2 tokens will be required in `Authorization` header:
```
Authorization: Bearer <token>
```

## Rate Limiting

**Current State**: No rate limiting implemented

**Future**: Rate limiting will be configured at API Gateway level.

## Versioning

**Current State**: No API versioning

**Future**: API versioning via URL path (`/api/v1/...`) or headers.

