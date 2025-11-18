package com.ftgo.orderservice.saga.steps;

import com.ftgo.orderservice.saga.AsyncSagaStep;
import com.ftgo.orderservice.saga.CreateOrderSagaData;
import com.ftgo.orderservice.saga.model.SagaStepResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

/**
 * Step 2: Create a kitchen ticket (Orchestration-based)
 * Compensating transaction: Cancel the ticket
 * 
 * This step is orchestrated by the SagaManager, not driven by events.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CreateTicketStep implements AsyncSagaStep {
    private final WebClient.Builder webClientBuilder;

    @Override
    public String getName() {
        return "CreateTicket";
    }

    @Override
    public void executeAsync(Object sagaData, Consumer<SagaStepResult> callback) {
        log.info("Executing CreateTicket step (orchestrated)");
        
        CreateOrderSagaData data = (CreateOrderSagaData) sagaData;
        
        // Call Kitchen Service synchronously via REST (orchestrated command)
        // The orchestrator waits for this to complete before proceeding
        webClientBuilder.build()
                .post()
                .uri("http://kitchen-service/tickets")
                .bodyValue(createTicketRequest(data))
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(ticketId -> {
                    log.info("Ticket created for order: {}, ticketId: {}", data.getOrderId(), ticketId);
                    // Report success back to orchestrator
                    callback.accept(SagaStepResult.success(data.getSagaInstanceId(), getName(), ticketId));
                })
                .doOnError(error -> {
                    log.error("Failed to create ticket for order: {}", data.getOrderId(), error);
                    // Report failure back to orchestrator
                    callback.accept(SagaStepResult.failure(data.getSagaInstanceId(), getName(), 
                            new Exception("Failed to create ticket", error)));
                })
                .subscribe();
    }

    @Override
    public boolean hasCompensation() {
        return true;
    }

    @Override
    public void compensateAsync(Object sagaData, Consumer<SagaStepResult> callback) {
        log.info("Compensating CreateTicket step (orchestrated)");
        
        CreateOrderSagaData data = (CreateOrderSagaData) sagaData;
        
        // Cancel the ticket in Kitchen Service via REST command
        if (data.getTicketId() != null) {
            webClientBuilder.build()
                    .delete()
                    .uri("http://kitchen-service/tickets/{ticketId}", data.getTicketId())
                    .retrieve()
                    .bodyToMono(Void.class)
                    .doOnSuccess(v -> {
                        log.info("Ticket cancelled for order: {}", data.getOrderId());
                        callback.accept(SagaStepResult.success(data.getSagaInstanceId(), getName(), null));
                    })
                    .doOnError(error -> {
                        log.error("Failed to cancel ticket for order: {}", data.getOrderId(), error);
                        callback.accept(SagaStepResult.failure(data.getSagaInstanceId(), getName(), 
                                new Exception("Failed to cancel ticket", error)));
                    })
                    .subscribe();
        } else {
            // No ticket was created, compensation not needed
            callback.accept(SagaStepResult.success(data.getSagaInstanceId(), getName(), null));
        }
    }

    private Object createTicketRequest(CreateOrderSagaData data) {
        // Create ticket request DTO from saga data
        // This would be a proper DTO in production
        return new TicketRequest(data.getOrderId(), data.getRestaurantId(), data.getLineItems());
    }

    // Inner class for ticket request
    private record TicketRequest(String orderId, String restaurantId, Object lineItems) {}
}

