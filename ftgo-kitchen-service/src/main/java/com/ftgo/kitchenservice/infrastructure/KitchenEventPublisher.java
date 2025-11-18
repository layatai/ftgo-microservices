package com.ftgo.kitchenservice.infrastructure;

import com.ftgo.common.events.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KitchenEventPublisher {
    private final StreamBridge streamBridge;

    public void publish(DomainEvent event) {
        log.info("Publishing event: {} with id: {}", event.getClass().getSimpleName(), event.getEventId());
        streamBridge.send("kitchenEvents-out-0", event);
    }
}

