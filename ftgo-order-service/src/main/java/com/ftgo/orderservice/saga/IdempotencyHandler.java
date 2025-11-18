package com.ftgo.orderservice.saga;

import com.ftgo.orderservice.saga.model.SagaInstance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Handles idempotency for saga operations.
 * Implements idempotent operations pattern from Chapter 4.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class IdempotencyHandler {
    private final SagaInstanceRepository sagaInstanceRepository;

    /**
     * Checks if a saga with the given idempotency key already exists.
     * If it does, returns the existing saga instance.
     * Otherwise, returns empty.
     */
    public Optional<SagaInstance> checkIdempotency(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return Optional.empty();
        }
        
        return sagaInstanceRepository.findByIdempotencyKey(idempotencyKey);
    }

    /**
     * Records an idempotency key for a saga instance.
     */
    public void recordIdempotencyKey(SagaInstance sagaInstance, String idempotencyKey) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            // Store idempotency key - in production, use a separate table or field
            log.info("Recording idempotency key: {} for saga: {}", idempotencyKey, sagaInstance.getId());
        }
    }
}

