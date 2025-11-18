package com.ftgo.common.events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TicketAcceptedEvent extends DomainEvent {
    private String ticketId;
    private String orderId;
    private String readyBy;

    public TicketAcceptedEvent(String ticketId, String orderId, String readyBy) {
        super(ticketId, "Ticket");
        this.ticketId = ticketId;
        this.orderId = orderId;
        this.readyBy = readyBy;
    }
}

