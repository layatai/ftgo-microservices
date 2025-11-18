package com.ftgo.orderservice.saga;

import com.ftgo.orderservice.saga.model.SagaStepResult;

import java.util.function.Consumer;

/**
 * Interface for async saga steps that report back to the orchestrator.
 * This replaces the synchronous SagaStep for orchestration-based sagas.
 */
public interface AsyncSagaStep {
    String getName();
    void executeAsync(Object sagaData, Consumer<SagaStepResult> callback);
    boolean hasCompensation();
    void compensateAsync(Object sagaData, Consumer<SagaStepResult> callback);
}

