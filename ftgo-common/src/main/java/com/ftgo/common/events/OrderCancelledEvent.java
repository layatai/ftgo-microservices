package com.ftgo.common.events;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderCancelledEvent extends DomainEvent {
    private String orderId;
    private String reason;

    public OrderCancelledEvent(String orderId, String reason) {
        super(orderId, "Order");
        this.orderId = orderId;
        this.reason = reason;
    }
}

