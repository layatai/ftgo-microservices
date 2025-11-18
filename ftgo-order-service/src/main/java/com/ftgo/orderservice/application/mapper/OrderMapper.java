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
    @Mapping(target = "orderTotal", source = "orderTotal", qualifiedByName = "moneyToString")
    @Mapping(target = "currency", source = "orderTotal", qualifiedByName = "moneyToCurrency")
    OrderDTO toDTO(Order order);
    
    @Mapping(target = "price", source = "price", qualifiedByName = "moneyToString")
    @Mapping(target = "currency", source = "price", qualifiedByName = "moneyToCurrency")
    OrderLineItemDTO toDTO(OrderLineItem lineItem);
    
    List<OrderLineItemDTO> toOrderLineItemDTOs(List<OrderLineItem> lineItems);
    
    @org.mapstruct.Named("moneyToString")
    default String moneyToString(Money money) {
        return money != null ? money.getAmount().toString() : null;
    }
    
    @org.mapstruct.Named("moneyToCurrency")
    default String moneyToCurrency(Money money) {
        return money != null ? money.getCurrency() : null;
    }
}

