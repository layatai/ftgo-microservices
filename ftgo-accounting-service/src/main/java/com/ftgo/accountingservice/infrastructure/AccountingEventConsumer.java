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

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountingEventConsumer {
    private final AccountingService accountingService;

    @Bean
    public Consumer<OrderApprovedEvent> orderApproved() {
        return event -> {
            log.info("Received OrderApprovedEvent for order: {}", event.getOrderId());
            // Create invoice when order is approved
            // This would typically fetch order details to get the amount
        };
    }

    @Bean
    public Consumer<DeliveryDeliveredEvent> deliveryDelivered() {
        return event -> {
            log.info("Received DeliveryDeliveredEvent for order: {}", event.getOrderId());
            // Process payment when delivery is completed
            // This would typically fetch invoice details
        };
    }
}

