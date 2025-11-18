package com.ftgo.orderservice.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrderLineItemRequest {
    @NotBlank(message = "Menu item ID is required")
    private String menuItemId;

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotBlank(message = "Price is required")
    private String price;

    @NotBlank(message = "Currency is required")
    private String currency;
}

