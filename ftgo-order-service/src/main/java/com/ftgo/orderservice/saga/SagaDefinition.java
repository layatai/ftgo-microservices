package com.ftgo.orderservice.saga;

import com.ftgo.orderservice.saga.model.SagaInstance;

import java.util.List;
import java.util.Optional;

/**
 * Defines a saga with its steps and execution order.
 * Base interface for saga definitions.
 * 
 * Uses AsyncSagaStep for orchestration-based sagas.
 */
public interface SagaDefinition {
    String getSagaType();
    List<AsyncSagaStep> getSteps();
    
    default Optional<AsyncSagaStep> findStep(String stepName) {
        return getSteps().stream()
                .filter(step -> step.getName().equals(stepName))
                .findFirst();
    }
    
    default Optional<AsyncSagaStep> getNextStep(SagaInstance sagaInstance) {
        List<String> completedSteps = sagaInstance.getCompletedStepNames();
        return getSteps().stream()
                .filter(step -> !completedSteps.contains(step.getName()))
                .findFirst();
    }
}

