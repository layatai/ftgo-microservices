package com.ftgo.restaurantservice.domain;

import java.util.Optional;

public interface RestaurantRepository {
    Restaurant save(Restaurant restaurant);
    Optional<Restaurant> findById(String id);
}

