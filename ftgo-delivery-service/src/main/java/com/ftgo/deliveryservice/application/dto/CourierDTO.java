package com.ftgo.deliveryservice.application.dto;

import lombok.Data;

@Data
public class CourierDTO {
    private String id;
    private String name;
    private String phoneNumber;
    private boolean available;
}

