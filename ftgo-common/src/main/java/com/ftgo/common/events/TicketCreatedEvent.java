package com.ftgo.common.events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TicketCreatedEvent extends DomainEvent {
    private String ticketId;
    private String orderId;
    private String restaurantId;
    private List<TicketLineItem> lineItems;
    private String readyBy;

    public TicketCreatedEvent(String ticketId, String orderId, String restaurantId, 
                            List<TicketLineItem> lineItems, String readyBy) {
        super(ticketId, "Ticket");
        this.ticketId = ticketId;
        this.orderId = orderId;
        this.restaurantId = restaurantId;
        this.lineItems = lineItems;
        this.readyBy = readyBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class TicketLineItem {
        private String menuItemId;
        private String name;
        private int quantity;
    }
}

