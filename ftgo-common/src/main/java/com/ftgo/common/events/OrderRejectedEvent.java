package com.ftgo.common.events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderRejectedEvent extends DomainEvent {
    private String orderId;
    private String reason;

    public OrderRejectedEvent(String orderId, String reason) {
        super(orderId, "Order");
        this.orderId = orderId;
        this.reason = reason;
    }
}

