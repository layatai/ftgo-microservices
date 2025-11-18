package com.ftgo.orderservice.infrastructure;

import com.ftgo.orderservice.application.dto.RestaurantMenuDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
@Slf4j
public class RestaurantServiceClient {
    private final WebClient.Builder webClientBuilder;
    private final CircuitBreakerFactory circuitBreakerFactory;

    public Mono<RestaurantMenuDTO> getRestaurantMenu(String restaurantId) {
        log.info("Fetching menu for restaurant: {}", restaurantId);
        
        Supplier<Mono<RestaurantMenuDTO>> supplier = () -> webClientBuilder.build()
                .get()
                .uri("http://restaurant-service/restaurants/{restaurantId}/menu", restaurantId)
                .retrieve()
                .bodyToMono(RestaurantMenuDTO.class);
        
        return circuitBreakerFactory.create("restaurant-service")
                .run(supplier, throwable -> {
                    log.error("Error fetching restaurant menu", throwable);
                    return Mono.error(throwable);
                });
    }
}

