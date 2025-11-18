package com.ftgo.accountingservice.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProcessPaymentRequest {
    @NotBlank(message = "Order ID is required")
    private String orderId;

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    @NotBlank(message = "Amount is required")
    private String amount;

    @NotBlank(message = "Currency is required")
    private String currency;

    @NotBlank(message = "Payment token is required")
    private String paymentToken;
}

