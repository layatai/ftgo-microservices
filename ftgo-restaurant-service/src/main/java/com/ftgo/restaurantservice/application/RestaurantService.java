package com.ftgo.restaurantservice.application;

import com.ftgo.common.domain.Money;
import com.ftgo.common.events.DomainEvent;
import com.ftgo.common.exception.EntityNotFoundException;
import com.ftgo.restaurantservice.domain.MenuItem;
import com.ftgo.restaurantservice.domain.Restaurant;
import com.ftgo.restaurantservice.domain.RestaurantRepository;
import com.ftgo.restaurantservice.infrastructure.RestaurantEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;
    private final RestaurantEventPublisher eventPublisher;

    @Transactional
    public Restaurant createRestaurant(String name, String address) {
        log.info("Creating restaurant: {}", name);
        Restaurant restaurant = new Restaurant(name, address);
        restaurant = restaurantRepository.save(restaurant);
        publishDomainEvents(restaurant);
        log.info("Created restaurant with id: {}", restaurant.getId());
        return restaurant;
    }

    @Transactional(readOnly = true)
    public Restaurant getRestaurant(String restaurantId) {
        log.info("Getting restaurant: {}", restaurantId);
        return restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found with id: " + restaurantId));
    }

    @Transactional
    public Restaurant updateMenu(String restaurantId, List<MenuItemDTO> menuItemDTOs) {
        log.info("Updating menu for restaurant: {}", restaurantId);
        Restaurant restaurant = getRestaurant(restaurantId);
        
        List<MenuItem> menuItems = menuItemDTOs.stream()
                .map(dto -> new MenuItem(dto.getName(), Money.of(dto.getPrice(), dto.getCurrency())))
                .collect(Collectors.toList());
        
        restaurant.updateMenu(menuItems);
        restaurant = restaurantRepository.save(restaurant);
        publishDomainEvents(restaurant);
        
        log.info("Updated menu for restaurant: {}", restaurantId);
        return restaurant;
    }

    private void publishDomainEvents(Restaurant restaurant) {
        List<DomainEvent> events = restaurant.getDomainEvents();
        events.forEach(eventPublisher::publish);
        restaurant.clearDomainEvents();
    }

    public record MenuItemDTO(String name, String price, String currency) {}
}

