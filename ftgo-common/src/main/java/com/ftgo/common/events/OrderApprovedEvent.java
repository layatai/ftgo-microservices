package com.ftgo.common.events;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderApprovedEvent extends DomainEvent {
    private String orderId;
    private String customerId;
    private String restaurantId;

    public OrderApprovedEvent(String orderId, String customerId, String restaurantId) {
        super(orderId, "Order");
        this.orderId = orderId;
        this.customerId = customerId;
        this.restaurantId = restaurantId;
    }
}

