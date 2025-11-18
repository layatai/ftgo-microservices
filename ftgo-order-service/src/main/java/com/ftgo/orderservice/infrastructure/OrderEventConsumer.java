package com.ftgo.orderservice.infrastructure;

import com.ftgo.common.events.MenuUpdatedEvent;
import com.ftgo.common.events.RestaurantCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {
    
    @Bean
    public Consumer<RestaurantCreatedEvent> restaurantCreated() {
        return event -> {
            log.info("Received RestaurantCreatedEvent: {}", event.getRestaurantId());
            // Handle restaurant created event if needed
        };
    }

    @Bean
    public Consumer<MenuUpdatedEvent> menuUpdated() {
        return event -> {
            log.info("Received MenuUpdatedEvent for restaurant: {}", event.getRestaurantId());
            // Handle menu updated event if needed
        };
    }
}

