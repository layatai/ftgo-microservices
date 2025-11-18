package com.ftgo.accountingservice.application.dto;

import lombok.Data;

@Data
public class InvoiceDTO {
    private String id;
    private String orderId;
    private String customerId;
    private String amount;
    private String currency;
}

