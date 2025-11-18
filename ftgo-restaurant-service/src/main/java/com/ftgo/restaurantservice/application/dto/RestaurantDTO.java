package com.ftgo.restaurantservice.application.dto;

import lombok.Data;
import java.util.List;

@Data
public class RestaurantDTO {
    private String id;
    private String name;
    private String address;
    private List<MenuItemDTO> menuItems;
}

