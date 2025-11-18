package com.ftgo.restaurantservice.infrastructure;

import com.ftgo.restaurantservice.domain.Restaurant;
import com.ftgo.restaurantservice.domain.RestaurantRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaRestaurantRepository extends JpaRepository<Restaurant, String>, RestaurantRepository {
}

