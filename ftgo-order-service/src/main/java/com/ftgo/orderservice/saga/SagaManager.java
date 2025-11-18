package com.ftgo.orderservice.saga;

import com.ftgo.orderservice.saga.model.SagaInstance;
import com.ftgo.orderservice.saga.model.SagaStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Manages saga instances and coordinates saga execution.
 * Implements the Orchestration-based Saga pattern from Chapter 4.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SagaManager {
    private final SagaInstanceRepository sagaInstanceRepository;
    private final List<SagaDefinition> sagaDefinitions;
    private final SagaStepRetryHandler retryHandler;

    public <T> SagaInstance createSagaInstance(String sagaType, T sagaData) {
        log.info("Creating saga instance of type: {}", sagaType);
        
        SagaDefinition sagaDefinition = findSagaDefinition(sagaType)
                .orElseThrow(() -> new IllegalArgumentException("Unknown saga type: " + sagaType));
        
        SagaInstance sagaInstance = new SagaInstance(sagaType, sagaData);
        sagaInstance = sagaInstanceRepository.save(sagaInstance);
        
        // Start the saga by executing the first step
        executeNextStep(sagaInstance);
        
        return sagaInstance;
    }

    public void handleStepResult(String sagaInstanceId, String stepName, Object result) {
        log.info("Handling step result for saga: {}, step: {}", sagaInstanceId, stepName);
        
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId)
                .orElseThrow(() -> new IllegalArgumentException("Saga instance not found: " + sagaInstanceId));
        
        final String sagaType = sagaInstance.getSagaType();
        final SagaDefinition sagaDefinition = findSagaDefinition(sagaType)
                .orElseThrow(() -> new IllegalArgumentException("Unknown saga type: " + sagaType));
        
        AsyncSagaStep step = sagaDefinition.findStep(stepName)
                .orElseThrow(() -> new IllegalArgumentException("Step not found: " + stepName));
        
        // Mark step as completed
        sagaInstance.completeStep(stepName, result);
        sagaInstance = sagaInstanceRepository.save(sagaInstance);
        
        // Execute next step or complete saga
        if (sagaInstance.isCompleted()) {
            log.info("Saga completed: {}", sagaInstanceId);
        } else {
            executeNextStep(sagaInstance);
        }
    }

    public void handleStepFailure(String sagaInstanceId, String stepName, Exception failure) {
        log.error("Handling step failure for saga: {}, step: {}", sagaInstanceId, stepName, failure);
        
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId)
                .orElseThrow(() -> new IllegalArgumentException("Saga instance not found: " + sagaInstanceId));
        
        SagaDefinition sagaDefinition = findSagaDefinition(sagaInstance.getSagaType())
                .orElseThrow(() -> new IllegalArgumentException("Unknown saga type: " + sagaInstance.getSagaType()));
        
        // Execute compensating transactions in reverse order
        compensateSaga(sagaInstance, sagaDefinition);
    }

    private void executeNextStep(SagaInstance sagaInstance) {
        final String sagaType = sagaInstance.getSagaType();
        final SagaDefinition sagaDefinition = findSagaDefinition(sagaType)
                .orElseThrow(() -> new IllegalArgumentException("Unknown saga type: " + sagaType));
        
        Optional<AsyncSagaStep> nextStep = sagaDefinition.getNextStep(sagaInstance);
        
        if (nextStep.isPresent()) {
            final AsyncSagaStep step = nextStep.get();
            final String sagaInstanceId = sagaInstance.getId();
            log.info("Executing next step: {} for saga: {} (orchestrated)", step.getName(), sagaInstanceId);
            
            sagaInstance.startStep(step.getName());
            sagaInstance = sagaInstanceRepository.save(sagaInstance);
            
            // Execute step asynchronously with retry and callback
            // Reload from database to ensure we have the latest state
            sagaInstance = sagaInstanceRepository.findById(sagaInstanceId)
                    .orElseThrow(() -> new IllegalStateException("Saga instance not found: " + sagaInstanceId));
            
            final CreateOrderSagaData sagaData = sagaInstance.getSagaData(CreateOrderSagaData.class);
            if (sagaData == null) {
                throw new IllegalStateException("Saga data is null for saga instance: " + sagaInstanceId);
            }
            sagaData.setSagaInstanceId(sagaInstanceId);
            
            // Use retry handler to execute step with Resilience4j retry
            retryHandler.executeWithRetry(step, sagaData, result -> {
                if (result.isSuccess()) {
                    // Store step result in saga data
                    storeStepResult(sagaData, step.getName(), result.getResult());
                    handleStepResult(sagaInstanceId, step.getName(), result.getResult());
                } else {
                    handleStepFailure(sagaInstanceId, step.getName(), result.getFailure());
                }
            });
        } else {
            sagaInstance.complete();
            sagaInstanceRepository.save(sagaInstance);
            log.info("Saga completed successfully: {}", sagaInstance.getId());
        }
    }
    
    private void storeStepResult(CreateOrderSagaData sagaData, String stepName, Object result) {
        // Store step results for compensation
        if ("CreateTicket".equals(stepName) && result != null) {
            sagaData.setTicketId(result.toString());
        } else if ("AuthorizeCard".equals(stepName) && result != null) {
            sagaData.setPaymentId(result.toString());
        }
    }

    private void compensateSaga(SagaInstance sagaInstance, SagaDefinition sagaDefinition) {
        log.info("Compensating saga: {}", sagaInstance.getId());
        
        sagaInstance.fail();
        sagaInstance = sagaInstanceRepository.save(sagaInstance);
        
        // Execute compensating transactions in reverse order
        final String sagaType = sagaInstance.getSagaType();
        final SagaDefinition sagaDef = findSagaDefinition(sagaType)
                .orElseThrow(() -> new IllegalArgumentException("Unknown saga type: " + sagaType));
        
        List<String> completedStepNames = sagaInstance.getCompletedStepNames();
        for (int i = completedStepNames.size() - 1; i >= 0; i--) {
            String stepName = completedStepNames.get(i);
            Optional<AsyncSagaStep> stepOpt = sagaDef.findStep(stepName);
            if (stepOpt.isPresent()) {
                AsyncSagaStep step = stepOpt.get();
                if (step.hasCompensation()) {
                    log.info("Executing compensation for step: {} (orchestrated)", step.getName());
                    CreateOrderSagaData sagaData = sagaInstance.getSagaData(CreateOrderSagaData.class);
                    sagaData.setSagaInstanceId(sagaInstance.getId());
                    
                    step.compensateAsync(sagaData, result -> {
                        if (result.isSuccess()) {
                            log.info("Compensation successful for step: {}", step.getName());
                        } else {
                            log.error("Compensation failed for step: {}", step.getName(), result.getFailure());
                            // Continue with other compensations even if one fails
                        }
                    });
                }
            }
        }
    }

    private Optional<SagaDefinition> findSagaDefinition(String sagaType) {
        return sagaDefinitions.stream()
                .filter(def -> def.getSagaType().equals(sagaType))
                .findFirst();
    }
}

