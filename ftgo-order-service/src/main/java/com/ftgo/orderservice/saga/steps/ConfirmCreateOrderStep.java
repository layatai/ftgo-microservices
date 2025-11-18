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
 * Step 4: Confirm order creation (final step) - Orchestration-based
 * No compensating transaction needed as this is the last step
 * 
 * This step is orchestrated by the SagaManager, not driven by events.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConfirmCreateOrderStep implements AsyncSagaStep {
    private final OrderRepository orderRepository;

    @Override
    public String getName() {
        return "ConfirmCreateOrder";
    }

    @Override
    public void executeAsync(Object sagaData, Consumer<SagaStepResult> callback) {
        log.info("Executing ConfirmCreateOrder step (orchestrated)");
        
        try {
            CreateOrderSagaData data = (CreateOrderSagaData) sagaData;
            Order order = orderRepository.findById(data.getOrderId())
                    .orElseThrow(() -> new IllegalStateException("Order not found: " + data.getOrderId()));
            
            // Order is already approved, just confirm
            log.info("Order creation confirmed: {}", data.getOrderId());
            callback.accept(SagaStepResult.success(data.getSagaInstanceId(), getName(), order.getId()));
        } catch (Exception e) {
            log.error("Order confirmation failed", e);
            CreateOrderSagaData data = (CreateOrderSagaData) sagaData;
            callback.accept(SagaStepResult.failure(data.getSagaInstanceId(), getName(), e));
        }
    }

    @Override
    public boolean hasCompensation() {
        return false; // Last step, no compensation needed
    }

    @Override
    public void compensateAsync(Object sagaData, Consumer<SagaStepResult> callback) {
        // No compensation for final step
        CreateOrderSagaData data = (CreateOrderSagaData) sagaData;
        callback.accept(SagaStepResult.success(data.getSagaInstanceId(), getName(), null));
    }
}

