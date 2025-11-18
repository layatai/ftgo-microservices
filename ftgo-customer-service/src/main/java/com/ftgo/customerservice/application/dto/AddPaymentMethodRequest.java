package com.ftgo.customerservice.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddPaymentMethodRequest {
    @NotBlank(message = "Payment token is required")
    private String paymentToken;
}

