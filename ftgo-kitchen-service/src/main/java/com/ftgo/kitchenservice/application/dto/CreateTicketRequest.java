package com.ftgo.kitchenservice.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class CreateTicketRequest {
    @NotBlank(message = "Order ID is required")
    private String orderId;

    @NotBlank(message = "Restaurant ID is required")
    private String restaurantId;

    @NotEmpty(message = "Line items cannot be empty")
    @Valid
    private List<CreateTicketLineItemRequest> lineItems;

    @NotBlank(message = "Ready by time is required")
    private String readyBy;
}

