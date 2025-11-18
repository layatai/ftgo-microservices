package com.ftgo.orderservice.saga.model;

public enum SagaState {
    STARTED,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    COMPENSATING,
    COMPENSATED
}

