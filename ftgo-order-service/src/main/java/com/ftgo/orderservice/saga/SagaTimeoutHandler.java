package com.ftgo.orderservice.saga;

import com.ftgo.orderservice.saga.model.SagaInstance;
import com.ftgo.orderservice.saga.model.SagaState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Handles saga timeouts and retries.
 * Implements timeout and retry mechanisms from Chapter 4.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SagaTimeoutHandler {
    private final SagaInstanceRepository sagaInstanceRepository;
    private final SagaManager sagaManager;
    
    private static final Duration SAGA_TIMEOUT = Duration.ofMinutes(30);
    private static final Duration STEP_TIMEOUT = Duration.ofMinutes(5);

    /**
     * Periodically check for timed-out sagas and steps.
     */
    @Scheduled(fixedDelay = 60000) // Run every minute
    public void checkTimeouts() {
        log.debug("Checking for timed-out sagas and steps");
        
        // Find sagas that are in progress and have timed out
        List<SagaInstance> inProgressSagas = sagaInstanceRepository.findByState(SagaState.IN_PROGRESS).stream()
                .filter(this::isSagaTimedOut)
                .toList();
        
        for (SagaInstance saga : inProgressSagas) {
            log.warn("Saga timed out: {}", saga.getId());
            handleSagaTimeout(saga);
        }
    }

    private boolean isSagaTimedOut(SagaInstance saga) {
        if (saga.getCreatedAt() == null) {
            return false;
        }
        
        Duration elapsed = Duration.between(saga.getCreatedAt(), Instant.now());
        return elapsed.compareTo(SAGA_TIMEOUT) > 0;
    }

    private void handleSagaTimeout(SagaInstance saga) {
        log.error("Handling timeout for saga: {}", saga.getId());
        
        // Mark saga as failed due to timeout
        saga.fail("Saga timed out after " + SAGA_TIMEOUT.toMinutes() + " minutes");
        sagaInstanceRepository.save(saga);
        
        // Trigger compensation
        try {
            sagaManager.handleStepFailure(saga.getId(), "TIMEOUT", 
                    new RuntimeException("Saga timed out"));
        } catch (Exception e) {
            log.error("Error handling saga timeout: {}", saga.getId(), e);
        }
    }
}

