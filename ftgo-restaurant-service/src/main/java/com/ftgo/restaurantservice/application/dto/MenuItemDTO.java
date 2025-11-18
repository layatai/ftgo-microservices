package com.ftgo.restaurantservice.application.dto;

import lombok.Data;

@Data
public class MenuItemDTO {
    private String id;
    private String name;
    private String price;
    private String currency;
}

