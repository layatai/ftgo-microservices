package com.ftgo.orderservice.saga.steps;

import com.ftgo.orderservice.saga.AsyncSagaStep;
import com.ftgo.orderservice.saga.CreateOrderSagaData;
import com.ftgo.orderservice.saga.model.SagaStepResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.function.Consumer;

/**
 * Step 3: Authorize the customer's credit card (Orchestration-based)
 * Compensating transaction: Release the authorization
 * 
 * This step is orchestrated by the SagaManager, not driven by events.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthorizeCardStep implements AsyncSagaStep {
    private final WebClient.Builder webClientBuilder;

    @Override
    public String getName() {
        return "AuthorizeCard";
    }

    @Override
    public void executeAsync(Object sagaData, Consumer<SagaStepResult> callback) {
        log.info("Executing AuthorizeCard step (orchestrated)");
        
        CreateOrderSagaData data = (CreateOrderSagaData) sagaData;
        
        // Call Accounting Service to authorize payment via REST (orchestrated command)
        webClientBuilder.build()
                .post()
                .uri("http://accounting-service/payments")
                .bodyValue(createPaymentRequest(data))
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(paymentId -> {
                    log.info("Card authorized for order: {}, paymentId: {}", data.getOrderId(), paymentId);
                    callback.accept(SagaStepResult.success(data.getSagaInstanceId(), getName(), paymentId));
                })
                .doOnError(error -> {
                    log.error("Failed to authorize card for order: {}", data.getOrderId(), error);
                    callback.accept(SagaStepResult.failure(data.getSagaInstanceId(), getName(), 
                            new Exception("Failed to authorize card", error)));
                })
                .subscribe();
    }

    @Override
    public boolean hasCompensation() {
        return true;
    }

    @Override
    public void compensateAsync(Object sagaData, Consumer<SagaStepResult> callback) {
        log.info("Compensating AuthorizeCard step (orchestrated)");
        
        CreateOrderSagaData data = (CreateOrderSagaData) sagaData;
        
        // Release the authorization via REST command
        if (data.getPaymentId() != null) {
            webClientBuilder.build()
                    .post()
                    .uri("http://accounting-service/payments/{paymentId}/release", data.getPaymentId())
                    .retrieve()
                    .bodyToMono(Void.class)
                    .doOnSuccess(v -> {
                        log.info("Card authorization released for order: {}", data.getOrderId());
                        callback.accept(SagaStepResult.success(data.getSagaInstanceId(), getName(), null));
                    })
                    .doOnError(error -> {
                        log.error("Failed to release authorization for order: {}", data.getOrderId(), error);
                        callback.accept(SagaStepResult.failure(data.getSagaInstanceId(), getName(), 
                                new Exception("Failed to release authorization", error)));
                    })
                    .subscribe();
        } else {
            // No authorization was made, compensation not needed
            callback.accept(SagaStepResult.success(data.getSagaInstanceId(), getName(), null));
        }
    }

    private Object createPaymentRequest(CreateOrderSagaData data) {
        // Create payment request DTO from saga data
        return new PaymentRequest(data.getOrderId(), data.getCustomerId(), data.getOrderTotal());
    }

    private record PaymentRequest(String orderId, String customerId, Object orderTotal) {}
}

