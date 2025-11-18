package com.ftgo.orderservice.saga.steps;

import com.ftgo.orderservice.domain.Order;
import com.ftgo.orderservice.domain.OrderRepository;
import com.ftgo.orderservice.saga.AsyncSagaStep;
import com.ftgo.orderservice.saga.CreateOrderSagaData;
import com.ftgo.orderservice.saga.model.SagaStepResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * Step 1: Validate the order (Orchestration-based)
 * Compensating transaction: Reject the order
 * 
 * This step is orchestrated by the SagaManager, not driven by events.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ValidateOrderStep implements AsyncSagaStep {
    private final OrderRepository orderRepository;

    @Override
    public String getName() {
        return "ValidateOrder";
    }

    @Override
    public void executeAsync(Object sagaData, Consumer<SagaStepResult> callback) {
        log.info("Executing ValidateOrder step (orchestrated)");
        
        try {
            CreateOrderSagaData data = (CreateOrderSagaData) sagaData;
            Order order = orderRepository.findById(data.getOrderId())
                    .orElseThrow(() -> new IllegalStateException("Order not found: " + data.getOrderId()));
            
            // Validate order (check customer, restaurant, menu items, etc.)
            // In a real implementation, this would call other services
            // For now, we'll just approve the order
            order.approve();
            orderRepository.save(order);
            
            log.info("Order validated and approved: {}", data.getOrderId());
            callback.accept(SagaStepResult.success(data.getSagaInstanceId(), getName(), order.getId()));
        } catch (Exception e) {
            log.error("Order validation failed", e);
            CreateOrderSagaData data = (CreateOrderSagaData) sagaData;
            callback.accept(SagaStepResult.failure(data.getSagaInstanceId(), getName(), e));
        }
    }

    @Override
    public boolean hasCompensation() {
        return true;
    }

    @Override
    public void compensateAsync(Object sagaData, Consumer<SagaStepResult> callback) {
        log.info("Compensating ValidateOrder step (orchestrated)");
        
        try {
            CreateOrderSagaData data = (CreateOrderSagaData) sagaData;
            Order order = orderRepository.findById(data.getOrderId())
                    .orElseThrow(() -> new IllegalStateException("Order not found: " + data.getOrderId()));
            
            order.reject("Order validation failed");
            orderRepository.save(order);
            
            log.info("Order rejected: {}", data.getOrderId());
            callback.accept(SagaStepResult.success(data.getSagaInstanceId(), getName(), null));
        } catch (Exception e) {
            log.error("Compensation failed for ValidateOrder step", e);
            CreateOrderSagaData data = (CreateOrderSagaData) sagaData;
            callback.accept(SagaStepResult.failure(data.getSagaInstanceId(), getName(), e));
        }
    }
}

