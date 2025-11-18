package com.ftgo.deliveryservice.application.mapper;

import com.ftgo.deliveryservice.application.dto.CourierDTO;
import com.ftgo.deliveryservice.application.dto.DeliveryDTO;
import com.ftgo.deliveryservice.domain.Courier;
import com.ftgo.deliveryservice.domain.Delivery;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DeliveryMapper {
    CourierDTO toDTO(Courier courier);
    DeliveryDTO toDTO(Delivery delivery);
}

