package com.ftgo.kitchenservice.infrastructure;

import com.ftgo.common.events.OrderApprovedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * Event consumer for Kitchen Service.
 * 
 * NOTE: This service now responds to orchestrated commands from Order Service
 * via REST API, not events. This consumer is kept for backward compatibility
 * but the primary communication is orchestrated.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KitchenEventConsumer {
    
    @Bean
    public Consumer<OrderApprovedEvent> orderApproved() {
        return event -> {
            log.info("Received OrderApprovedEvent for order: {} (legacy event - orchestration preferred)", 
                    event.getOrderId());
            // In orchestration-based pattern, tickets are created via REST commands
            // from the Order Service orchestrator, not via events
        };
    }
}
