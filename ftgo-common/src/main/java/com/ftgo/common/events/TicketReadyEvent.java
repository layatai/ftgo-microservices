package com.ftgo.common.events;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketReadyEvent extends DomainEvent {
    private String ticketId;
    private String orderId;
    private String readyBy;

    public TicketReadyEvent(String ticketId, String orderId, String readyBy) {
        super(ticketId, "Ticket");
        this.ticketId = ticketId;
        this.orderId = orderId;
        this.readyBy = readyBy;
    }
}

