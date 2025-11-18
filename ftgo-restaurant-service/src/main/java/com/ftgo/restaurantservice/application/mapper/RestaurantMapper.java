package com.ftgo.restaurantservice.application.mapper;

import com.ftgo.common.domain.Money;
import com.ftgo.restaurantservice.application.dto.MenuItemDTO;
import com.ftgo.restaurantservice.application.dto.RestaurantDTO;
import com.ftgo.restaurantservice.domain.MenuItem;
import com.ftgo.restaurantservice.domain.Restaurant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RestaurantMapper {
    RestaurantDTO toDTO(Restaurant restaurant);
    
    MenuItemDTO toDTO(MenuItem menuItem);
    
    List<MenuItemDTO> toMenuItemDTOs(List<MenuItem> menuItems);
    
    default String mapMoney(Money money) {
        return money != null ? money.getAmount().toString() : null;
    }
}

