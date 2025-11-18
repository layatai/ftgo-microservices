package com.ftgo.common.events;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeliveryPickedUpEvent extends DomainEvent {
    private String deliveryId;
    private String orderId;
    private String pickedUpAt;

    public DeliveryPickedUpEvent(String deliveryId, String orderId, String pickedUpAt) {
        super(deliveryId, "Delivery");
        this.deliveryId = deliveryId;
        this.orderId = orderId;
        this.pickedUpAt = pickedUpAt;
    }
}

