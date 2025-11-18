package com.ftgo.customerservice.application.dto;

import lombok.Data;

@Data
public class PaymentMethodDTO {
    private String id;
    private String paymentToken;
    private boolean active;
}

