package com.ftgo.deliveryservice.infrastructure;

import com.ftgo.common.events.TicketReadyEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeliveryEventConsumer {
    
    @Bean
    public Consumer<TicketReadyEvent> ticketReady() {
        return event -> {
            log.info("Received TicketReadyEvent for order: {}", event.getOrderId());
            // When ticket is ready, create a delivery
            // This would typically be handled by the service layer
        };
    }
}

