package com.ftgo.common.events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class OrderCreatedEvent extends DomainEvent {
    private String orderId;
    private String customerId;
    private String restaurantId;
    private String orderState;
    private List<OrderLineItem> lineItems;
    private String deliveryAddress;
    private String deliveryTime;

    public OrderCreatedEvent(String orderId, String customerId, String restaurantId, 
                           String orderState, List<OrderLineItem> lineItems, 
                           String deliveryAddress, String deliveryTime) {
        super(orderId, "Order");
        this.orderId = orderId;
        this.customerId = customerId;
        this.restaurantId = restaurantId;
        this.orderState = orderState;
        this.lineItems = lineItems;
        this.deliveryAddress = deliveryAddress;
        this.deliveryTime = deliveryTime;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class OrderLineItem {
        private String menuItemId;
        private String name;
        private int quantity;
        private String price;
        private String currency;
    }
}

