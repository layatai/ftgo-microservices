package com.ftgo.orderservice.saga.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Tracks the execution of a single saga step.
 */
@Entity
@Table(name = "saga_step_executions")
@Getter
@NoArgsConstructor
public class SagaStepExecution {
    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saga_instance_id", nullable = false)
    private SagaInstance sagaInstance;

    @Column(nullable = false)
    private String stepName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StepExecutionState state;

    @Column(columnDefinition = "TEXT")
    private String result;

    private Instant startedAt;
    private Instant completedAt;
    private String failureReason;

    public SagaStepExecution(SagaInstance sagaInstance, String stepName) {
        this.id = UUID.randomUUID().toString();
        this.sagaInstance = sagaInstance;
        this.stepName = stepName;
        this.state = StepExecutionState.STARTED;
        this.startedAt = Instant.now();
    }

    public void complete(Object result) {
        this.state = StepExecutionState.COMPLETED;
        this.result = result != null ? result.toString() : null;
        this.completedAt = Instant.now();
    }

    public void fail(String reason) {
        this.state = StepExecutionState.FAILED;
        this.failureReason = reason;
        this.completedAt = Instant.now();
    }

    public void compensate() {
        this.state = StepExecutionState.COMPENSATED;
    }
}

