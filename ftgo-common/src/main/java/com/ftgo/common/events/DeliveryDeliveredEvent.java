package com.ftgo.common.events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DeliveryDeliveredEvent extends DomainEvent {
    private String deliveryId;
    private String orderId;
    private String deliveredAt;

    public DeliveryDeliveredEvent(String deliveryId, String orderId, String deliveredAt) {
        super(deliveryId, "Delivery");
        this.deliveryId = deliveryId;
        this.orderId = orderId;
        this.deliveredAt = deliveredAt;
    }
}

