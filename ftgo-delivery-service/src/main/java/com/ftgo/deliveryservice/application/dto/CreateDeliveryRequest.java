package com.ftgo.deliveryservice.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateDeliveryRequest {
    @NotBlank(message = "Order ID is required")
    private String orderId;

    @NotBlank(message = "Courier ID is required")
    private String courierId;

    @NotBlank(message = "Pickup address is required")
    private String pickupAddress;

    @NotBlank(message = "Delivery address is required")
    private String deliveryAddress;

    @NotBlank(message = "Pickup time is required")
    private String pickupTime;
}

