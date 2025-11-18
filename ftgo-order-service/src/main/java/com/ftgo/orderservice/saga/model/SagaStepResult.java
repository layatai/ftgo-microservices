package com.ftgo.orderservice.saga.model;

import lombok.Value;

/**
 * Represents the result of a saga step execution.
 * Used to report back to the orchestrator.
 */
@Value
public class SagaStepResult {
    String sagaInstanceId;
    String stepName;
    boolean success;
    Object result;
    Exception failure;
    
    public boolean isSuccess() {
        return success;
    }

    public static SagaStepResult success(String sagaInstanceId, String stepName, Object result) {
        return new SagaStepResult(sagaInstanceId, stepName, true, result, null);
    }

    public static SagaStepResult failure(String sagaInstanceId, String stepName, Exception failure) {
        return new SagaStepResult(sagaInstanceId, stepName, false, null, failure);
    }
}

