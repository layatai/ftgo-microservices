package com.ftgo.orderservice.application.dto;

import com.ftgo.orderservice.domain.OrderState;
import lombok.Data;
import java.util.List;

@Data
public class OrderDTO {
    private String id;
    private String customerId;
    private String restaurantId;
    private OrderState state;
    private List<OrderLineItemDTO> lineItems;
    private String orderTotal;
    private String currency;
    private String deliveryAddress;
    private String deliveryTime;
}

