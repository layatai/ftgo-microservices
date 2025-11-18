package com.ftgo.orderservice.saga.model;

/**
 * Represents a single step in a saga.
 * Each step has an action and optionally a compensating action.
 */
public interface SagaStep {
    String getName();
    void execute(Object sagaData) throws Exception;
    boolean hasCompensation();
    void compensate(Object sagaData) throws Exception;
}

