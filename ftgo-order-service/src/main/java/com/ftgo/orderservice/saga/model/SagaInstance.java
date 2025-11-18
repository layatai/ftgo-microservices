package com.ftgo.orderservice.saga.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a saga instance with its state and execution history.
 * Implements saga state management as described in Chapter 4.
 */
@Entity
@Table(name = "saga_instances")
@Getter
@NoArgsConstructor
public class SagaInstance {
    @Id
    private String id;

    @Column(nullable = false)
    private String sagaType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SagaState state;

    @Column(columnDefinition = "TEXT")
    private String sagaData; // JSON serialized saga data

    @Column(nullable = false)
    private Instant createdAt;

    private Instant completedAt;
    private String failureReason;
    
    @Column(unique = true)
    private String idempotencyKey;

    @OneToMany(mappedBy = "sagaInstance", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SagaStepExecution> stepExecutions = new ArrayList<>();

    @Transient
    private List<String> completedStepNames;

    public SagaInstance(String sagaType, Object sagaData) {
        this.id = UUID.randomUUID().toString();
        this.sagaType = sagaType;
        this.state = SagaState.STARTED;
        this.sagaData = serializeSagaData(sagaData);
        this.createdAt = Instant.now();
    }

    public void startStep(String stepName) {
        SagaStepExecution execution = new SagaStepExecution(this, stepName);
        this.stepExecutions.add(execution);
        this.state = SagaState.IN_PROGRESS;
    }

    public void completeStep(String stepName, Object result) {
        SagaStepExecution execution = findStepExecution(stepName)
                .orElseThrow(() -> new IllegalStateException("Step not found: " + stepName));
        execution.complete(result);
        if (completedStepNames == null) {
            completedStepNames = new ArrayList<>();
        }
        if (!completedStepNames.contains(stepName)) {
            completedStepNames.add(stepName);
        }
    }

    public void complete() {
        this.state = SagaState.COMPLETED;
        this.completedAt = Instant.now();
    }

    public void fail() {
        this.state = SagaState.FAILED;
        this.completedAt = Instant.now();
    }

    public void fail(String reason) {
        this.failureReason = reason;
        fail();
    }

    public boolean isCompleted() {
        return this.state == SagaState.COMPLETED || this.state == SagaState.FAILED;
    }

    public List<String> getCompletedStepNames() {
        if (completedStepNames == null) {
            completedStepNames = stepExecutions.stream()
                    .filter(exec -> exec.getState() == StepExecutionState.COMPLETED)
                    .map(SagaStepExecution::getStepName)
                    .toList();
        }
        return completedStepNames;
    }

    private java.util.Optional<SagaStepExecution> findStepExecution(String stepName) {
        return stepExecutions.stream()
                .filter(exec -> exec.getStepName().equals(stepName))
                .findFirst();
    }

    private String serializeSagaData(Object sagaData) {
        // Simple serialization - in production, use Jackson or similar
        return sagaData != null ? sagaData.toString() : null;
    }

    @SuppressWarnings("unchecked")
    public <T> T getSagaData() {
        // Deserialize saga data - in production, use Jackson or similar
        // For now, return the string representation
        return (T) sagaData;
    }
}

