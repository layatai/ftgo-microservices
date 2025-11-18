package com.ftgo.deliveryservice.application.dto;

import com.ftgo.deliveryservice.domain.DeliveryState;
import lombok.Data;

@Data
public class DeliveryDTO {
    private String id;
    private String orderId;
    private String courierId;
    private DeliveryState state;
    private String pickupAddress;
    private String deliveryAddress;
    private String pickupTime;
    private String pickedUpAt;
    private String deliveredAt;
}

