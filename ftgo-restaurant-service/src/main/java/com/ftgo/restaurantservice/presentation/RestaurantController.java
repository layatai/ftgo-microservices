package com.ftgo.restaurantservice.presentation;

import com.ftgo.common.exception.EntityNotFoundException;
import com.ftgo.restaurantservice.application.RestaurantService;
import com.ftgo.restaurantservice.application.dto.CreateRestaurantRequest;
import com.ftgo.restaurantservice.application.dto.MenuItemDTO;
import com.ftgo.restaurantservice.application.dto.RestaurantDTO;
import com.ftgo.restaurantservice.application.dto.UpdateMenuRequest;
import com.ftgo.restaurantservice.application.mapper.RestaurantMapper;
import com.ftgo.restaurantservice.domain.Restaurant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/restaurants")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Restaurant", description = "Restaurant management APIs")
public class RestaurantController {
    private final RestaurantService restaurantService;
    private final RestaurantMapper restaurantMapper;

    @GetMapping
    @Operation(summary = "Get all restaurants")
    public ResponseEntity<List<RestaurantDTO>> getAllRestaurants() {
        log.info("Getting all restaurants");
        List<Restaurant> restaurants = restaurantService.getAllRestaurants();
        List<RestaurantDTO> restaurantDTOs = restaurants.stream()
                .map(restaurantMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(restaurantDTOs);
    }

    @PostMapping
    @Operation(summary = "Create a new restaurant")
    public ResponseEntity<RestaurantDTO> createRestaurant(@Valid @RequestBody CreateRestaurantRequest request) {
        log.info("Creating restaurant: {}", request.getName());
        Restaurant restaurant = restaurantService.createRestaurant(request.getName(), request.getAddress());
        return ResponseEntity.status(HttpStatus.CREATED).body(restaurantMapper.toDTO(restaurant));
    }

    @GetMapping("/{restaurantId}")
    @Operation(summary = "Get restaurant details")
    public ResponseEntity<RestaurantDTO> getRestaurant(@PathVariable String restaurantId) {
        log.info("Getting restaurant: {}", restaurantId);
        Restaurant restaurant = restaurantService.getRestaurant(restaurantId);
        return ResponseEntity.ok(restaurantMapper.toDTO(restaurant));
    }

    @GetMapping("/{restaurantId}/menu")
    @Operation(summary = "Get restaurant menu")
    public ResponseEntity<List<MenuItemDTO>> getMenu(@PathVariable String restaurantId) {
        log.info("Getting menu for restaurant: {}", restaurantId);
        Restaurant restaurant = restaurantService.getRestaurant(restaurantId);
        return ResponseEntity.ok(restaurantMapper.toMenuItemDTOs(restaurant.getMenuItems()));
    }

    @PutMapping("/{restaurantId}/menu")
    @Operation(summary = "Update restaurant menu")
    public ResponseEntity<RestaurantDTO> updateMenu(
            @PathVariable String restaurantId,
            @Valid @RequestBody UpdateMenuRequest request) {
        log.info("Updating menu for restaurant: {}", restaurantId);
        
        List<RestaurantService.MenuItemDTO> menuItemDTOs = request.getMenuItems().stream()
                .map(item -> new RestaurantService.MenuItemDTO(item.getName(), item.getPrice(), item.getCurrency()))
                .collect(Collectors.toList());
        
        Restaurant restaurant = restaurantService.updateMenu(restaurantId, menuItemDTOs);
        return ResponseEntity.ok(restaurantMapper.toDTO(restaurant));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}

