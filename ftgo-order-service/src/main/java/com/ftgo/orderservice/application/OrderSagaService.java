package com.ftgo.orderservice.application;

import com.ftgo.orderservice.saga.CreateOrderSagaData;
import com.ftgo.orderservice.saga.IdempotencyHandler;
import com.ftgo.orderservice.saga.SagaManager;
import com.ftgo.orderservice.saga.SemanticLockManager;
import com.ftgo.orderservice.saga.model.SagaInstance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing order creation through sagas.
 * Implements the Create Order Saga from Chapter 4.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderSagaService {
    private final SagaManager sagaManager;
    private final IdempotencyHandler idempotencyHandler;
    private final SemanticLockManager semanticLockManager;

    @Transactional
    public SagaInstance createOrderSaga(CreateOrderSagaData sagaData) {
        log.info("Creating order saga for order: {}", sagaData.getOrderId());
        
        // Check idempotency
        if (sagaData.getIdempotencyKey() != null) {
            Optional<SagaInstance> existingSaga = idempotencyHandler.checkIdempotency(
                    sagaData.getIdempotencyKey());
            if (existingSaga.isPresent()) {
                log.info("Saga already exists for idempotency key: {}", sagaData.getIdempotencyKey());
                return existingSaga.get();
            }
        } else {
            // Generate idempotency key if not provided
            sagaData.setIdempotencyKey(UUID.randomUUID().toString());
        }
        
        // Acquire semantic locks
        if (!semanticLockManager.acquireLock("Order", sagaData.getOrderId(), 
                sagaData.getIdempotencyKey())) {
            throw new IllegalStateException("Order is locked by another saga");
        }
        
        try {
            // Create saga instance
            SagaInstance sagaInstance = sagaManager.createSagaInstance(
                    "CreateOrderSaga", sagaData);
            
            // Record idempotency key
            idempotencyHandler.recordIdempotencyKey(sagaInstance, sagaData.getIdempotencyKey());
            
            return sagaInstance;
        } catch (Exception e) {
            // Release locks on failure
            semanticLockManager.releaseAllLocks(sagaData.getIdempotencyKey());
            throw e;
        }
    }
}

