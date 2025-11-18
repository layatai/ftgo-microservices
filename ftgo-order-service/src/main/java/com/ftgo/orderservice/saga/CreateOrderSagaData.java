package com.ftgo.orderservice.saga;

import lombok.Data;

/**
 * Data structure for Create Order Saga.
 * Contains all information needed to execute the saga.
 */
@Data
public class CreateOrderSagaData {
    private String sagaInstanceId; // Added for orchestration
    private String orderId;
    private String customerId;
    private String restaurantId;
    private Object ticketRequest;
    private Object paymentRequest;
    private Object lineItems;
    private Object orderTotal;
    
    // Step results stored for compensation
    private String ticketId;
    private String paymentId;
    
    // For idempotency
    private String idempotencyKey;
}

