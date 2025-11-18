package com.ftgo.accountingservice.infrastructure;

import com.ftgo.common.domain.Money;
import com.ftgo.common.events.DeliveryDeliveredEvent;
import com.ftgo.common.events.OrderApprovedEvent;
import com.ftgo.accountingservice.application.AccountingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * Event consumer for Accounting Service.
 * 
 * NOTE: This service now responds to orchestrated commands from Order Service
 * via REST API, not events. This consumer is kept for backward compatibility
 * but the primary communication is orchestrated.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AccountingEventConsumer {
    private final AccountingService accountingService;

    @Bean
    public Consumer<OrderApprovedEvent> orderApproved() {
        return event -> {
            log.info("Received OrderApprovedEvent for order: {} (legacy event - orchestration preferred)", 
                    event.getOrderId());
            // In orchestration-based pattern, payments are authorized via REST commands
            // from the Order Service orchestrator, not via events
        };
    }

    @Bean
    public Consumer<DeliveryDeliveredEvent> deliveryDelivered() {
        return event -> {
            log.info("Received DeliveryDeliveredEvent for order: {} (legacy event - orchestration preferred)", 
                    event.getOrderId());
            // In orchestration-based pattern, payment processing is done via REST commands
            // from the Order Service orchestrator, not via events
        };
    }
}
