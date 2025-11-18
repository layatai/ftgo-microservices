package com.ftgo.orderservice.application.mapper;

import com.ftgo.common.domain.Money;
import com.ftgo.orderservice.application.dto.OrderDTO;
import com.ftgo.orderservice.application.dto.OrderLineItemDTO;
import com.ftgo.orderservice.domain.Order;
import com.ftgo.orderservice.domain.OrderLineItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    OrderDTO toDTO(Order order);
    
    @Mapping(target = "order", ignore = true)
    OrderLineItemDTO toDTO(OrderLineItem lineItem);
    
    List<OrderLineItemDTO> toOrderLineItemDTOs(List<OrderLineItem> lineItems);
    
    default String mapMoney(Money money) {
        return money != null ? money.getAmount().toString() : null;
    }
    
    default String mapCurrency(Money money) {
        return money != null ? money.getCurrency() : null;
    }
}

