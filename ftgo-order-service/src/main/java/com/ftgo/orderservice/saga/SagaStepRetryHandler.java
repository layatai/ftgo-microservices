package com.ftgo.orderservice.saga;

import com.ftgo.orderservice.saga.model.SagaStepResult;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Handles retry logic for saga steps using Resilience4j Retry with exponential backoff.
 * Wraps async callback-based saga step execution in CompletableFuture to integrate with Resilience4j.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SagaStepRetryHandler {
    private static final String RETRY_INSTANCE_NAME = "saga-step-retry";
    
    private final RetryRegistry retryRegistry;

    /**
     * Executes a saga step with retry logic and exponential backoff using Resilience4j.
     * 
     * @param step The saga step to execute
     * @param sagaData The saga data
     * @param callback The callback to invoke with the final result
     */
    public void executeWithRetry(AsyncSagaStep step, Object sagaData, Consumer<SagaStepResult> callback) {
        log.debug("Executing step: {} with Resilience4j retry", step.getName());
        
        // Get retry instance from registry
        Retry retry = retryRegistry.retry(RETRY_INSTANCE_NAME);
        
        // Extract saga instance ID for error reporting
        String sagaInstanceId = extractSagaInstanceId(sagaData);
        
        // Create a supplier that wraps the async step execution in CompletableFuture
        // This supplier will be called for each retry attempt
        Supplier<CompletableFuture<SagaStepResult>> stepSupplier = () -> {
            CompletableFuture<SagaStepResult> future = new CompletableFuture<>();
            
            log.debug("Attempting to execute step: {}", step.getName());
            
            // Execute step asynchronously - this will be called for each retry
            step.executeAsync(sagaData, result -> {
                if (result.isSuccess()) {
                    log.debug("Step {} succeeded", step.getName());
                    future.complete(result);
                } else {
                    // If failure, complete exceptionally so Resilience4j can retry
                    Exception failure = result.getFailure();
                    log.debug("Step {} failed: {}", step.getName(), failure.getMessage());
                    future.completeExceptionally(failure);
                }
            });
            
            return future;
        };
        
        // Decorate with retry logic - Resilience4j will call the supplier for each retry attempt
        CompletableFuture<SagaStepResult> retryableFuture = retry.executeCompletionStage(stepSupplier);
        
        // Handle final result (success or failure after retries)
        retryableFuture
            .whenComplete((result, throwable) -> {
                if (throwable != null) {
                    // All retries exhausted, convert exception back to SagaStepResult
                    Exception finalException = throwable instanceof Exception 
                        ? (Exception) throwable 
                        : new Exception("Step execution failed", throwable);
                    
                    // Get the root cause if available
                    Throwable cause = finalException.getCause();
                    if (cause instanceof Exception) {
                        finalException = (Exception) cause;
                    }
                    
                    SagaStepResult failureResult = SagaStepResult.failure(
                        sagaInstanceId, 
                        step.getName(), 
                        finalException
                    );
                    
                    log.error("Step {} failed after all retry attempts", step.getName(), finalException);
                    callback.accept(failureResult);
                } else {
                    // Success
                    log.debug("Step {} completed successfully", step.getName());
                    callback.accept(result);
                }
            });
    }
    
    /**
     * Extracts saga instance ID from saga data for error reporting.
     */
    private String extractSagaInstanceId(Object sagaData) {
        if (sagaData instanceof CreateOrderSagaData) {
            CreateOrderSagaData data = (CreateOrderSagaData) sagaData;
            return data.getSagaInstanceId() != null ? data.getSagaInstanceId() : "unknown";
        }
        return "unknown";
    }
}
