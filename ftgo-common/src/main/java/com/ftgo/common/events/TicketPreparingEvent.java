package com.ftgo.common.events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TicketPreparingEvent extends DomainEvent {
    private String ticketId;
    private String orderId;

    public TicketPreparingEvent(String ticketId, String orderId) {
        super(ticketId, "Ticket");
        this.ticketId = ticketId;
        this.orderId = orderId;
    }
}

