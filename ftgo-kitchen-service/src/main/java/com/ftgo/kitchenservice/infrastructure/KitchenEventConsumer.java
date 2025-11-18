package com.ftgo.kitchenservice.infrastructure;

import com.ftgo.common.events.OrderApprovedEvent;
import com.ftgo.common.events.OrderCreatedEvent;
import com.ftgo.kitchenservice.application.KitchenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class KitchenEventConsumer {
    private final KitchenService kitchenService;

    @Bean
    public Consumer<OrderApprovedEvent> orderApproved() {
        return event -> {
            log.info("Received OrderApprovedEvent for order: {}", event.getOrderId());
            // When order is approved, create a ticket
            // This would typically fetch order details, but for simplicity we'll handle it in the controller
        };
    }
}

