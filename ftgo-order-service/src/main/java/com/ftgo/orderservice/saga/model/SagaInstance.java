package com.ftgo.orderservice.saga.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class SagaInstance {
    private static final ObjectMapper objectMapper = createObjectMapper();
    
    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Configure to handle circular references and JPA entities
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // Disable features that cause issues with JPA entities
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
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
        if (sagaData == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(sagaData);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize saga data", e);
            throw new RuntimeException("Failed to serialize saga data", e);
        }
    }

    /**
     * Get saga data with explicit type.
     * This method should be used when you know the exact type of the saga data.
     */
    public <T> T getSagaData(Class<T> clazz) {
        if (sagaData == null || sagaData.isBlank()) {
            log.warn("Saga data is null or blank for saga instance: {}", id);
            return null;
        }
        try {
            log.debug("Deserializing saga data to {}: {}", clazz.getName(), sagaData);
            T result = objectMapper.readValue(sagaData, clazz);
            log.debug("Successfully deserialized saga data to {}", clazz.getName());
            return result;
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize saga data to {}: {}", clazz.getName(), sagaData, e);
            throw new RuntimeException("Failed to deserialize saga data to " + clazz.getName() + ": " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error deserializing saga data to {}: {}", clazz.getName(), sagaData, e);
            throw new RuntimeException("Unexpected error deserializing saga data to " + clazz.getName() + ": " + e.getMessage(), e);
        }
    }
}

