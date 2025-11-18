package com.ftgo.kitchenservice.application.dto;

import com.ftgo.kitchenservice.domain.TicketState;
import lombok.Data;
import java.util.List;

@Data
public class TicketDTO {
    private String id;
    private String orderId;
    private String restaurantId;
    private TicketState state;
    private List<TicketLineItemDTO> lineItems;
    private String readyBy;
}

