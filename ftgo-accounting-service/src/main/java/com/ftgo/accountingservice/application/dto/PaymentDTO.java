package com.ftgo.accountingservice.application.dto;

import com.ftgo.accountingservice.domain.PaymentStatus;
import lombok.Data;

@Data
public class PaymentDTO {
    private String id;
    private String orderId;
    private String customerId;
    private String amount;
    private String currency;
    private PaymentStatus status;
    private String paymentToken;
}

