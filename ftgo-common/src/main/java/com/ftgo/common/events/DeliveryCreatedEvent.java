package com.ftgo.common.events;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeliveryCreatedEvent extends DomainEvent {
    private String deliveryId;
    private String orderId;
    private String courierId;
    private String pickupAddress;
    private String deliveryAddress;
    private String pickupTime;

    public DeliveryCreatedEvent(String deliveryId, String orderId, String courierId, 
                              String pickupAddress, String deliveryAddress, String pickupTime) {
        super(deliveryId, "Delivery");
        this.deliveryId = deliveryId;
        this.orderId = orderId;
        this.courierId = courierId;
        this.pickupAddress = pickupAddress;
        this.deliveryAddress = deliveryAddress;
        this.pickupTime = pickupTime;
    }
}

