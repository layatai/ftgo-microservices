package com.ftgo.kitchenservice.application.mapper;

import com.ftgo.kitchenservice.application.dto.TicketDTO;
import com.ftgo.kitchenservice.application.dto.TicketLineItemDTO;
import com.ftgo.kitchenservice.domain.Ticket;
import com.ftgo.kitchenservice.domain.TicketLineItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TicketMapper {
    TicketDTO toDTO(Ticket ticket);
    
    @Mapping(target = "ticket", ignore = true)
    TicketLineItemDTO toDTO(TicketLineItem lineItem);
    
    List<TicketLineItemDTO> toTicketLineItemDTOs(List<TicketLineItem> lineItems);
}

