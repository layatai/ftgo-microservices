package com.ftgo.orderservice.saga;

import com.ftgo.orderservice.saga.model.SagaInstance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Event-driven saga orchestrator that handles saga step results via events.
 * This allows for asynchronous saga execution.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EventDrivenSagaOrchestrator {
    private final SagaManager sagaManager;

    @EventListener
    public void handleSagaStepResult(SagaStepResultEvent event) {
        log.info("Received saga step result event: {}", event);
        sagaManager.handleStepResult(
                event.getSagaInstanceId(),
                event.getStepName(),
                event.getResult()
        );
    }

    @EventListener
    public void handleSagaStepFailure(SagaStepFailureEvent event) {
        log.error("Received saga step failure event: {}", event);
        sagaManager.handleStepFailure(
                event.getSagaInstanceId(),
                event.getStepName(),
                event.getFailure()
        );
    }

    public static class SagaStepResultEvent {
        private final String sagaInstanceId;
        private final String stepName;
        private final Object result;

        public SagaStepResultEvent(String sagaInstanceId, String stepName, Object result) {
            this.sagaInstanceId = sagaInstanceId;
            this.stepName = stepName;
            this.result = result;
        }

        public String getSagaInstanceId() { return sagaInstanceId; }
        public String getStepName() { return stepName; }
        public Object getResult() { return result; }
    }

    public static class SagaStepFailureEvent {
        private final String sagaInstanceId;
        private final String stepName;
        private final Exception failure;

        public SagaStepFailureEvent(String sagaInstanceId, String stepName, Exception failure) {
            this.sagaInstanceId = sagaInstanceId;
            this.stepName = stepName;
            this.failure = failure;
        }

        public String getSagaInstanceId() { return sagaInstanceId; }
        public String getStepName() { return stepName; }
        public Exception getFailure() { return failure; }
    }
}

