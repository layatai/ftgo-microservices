package com.ftgo.orderservice.saga;

import com.ftgo.orderservice.saga.model.SagaInstance;
import com.ftgo.orderservice.saga.model.SagaState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SagaInstanceRepository extends JpaRepository<SagaInstance, String> {
    Optional<SagaInstance> findByIdempotencyKey(String idempotencyKey);
    
    List<SagaInstance> findByState(SagaState state);
}

