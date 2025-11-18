package com.ftgo.orderservice.application.dto;

import lombok.Data;
import java.util.List;

@Data
public class RestaurantMenuDTO {
    private List<MenuItemDTO> menuItems;
    
    @Data
    public static class MenuItemDTO {
        private String id;
        private String name;
        private String price;
        private String currency;
    }
}

