package com.ftgo.kitchenservice.application.dto;

import lombok.Data;

@Data
public class TicketLineItemDTO {
    private String id;
    private String menuItemId;
    private String name;
    private int quantity;
}

