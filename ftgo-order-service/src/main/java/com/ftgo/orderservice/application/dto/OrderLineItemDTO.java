package com.ftgo.orderservice.application.dto;

import lombok.Data;

@Data
public class OrderLineItemDTO {
    private String id;
    private String menuItemId;
    private String name;
    private int quantity;
    private String price;
    private String currency;
}

