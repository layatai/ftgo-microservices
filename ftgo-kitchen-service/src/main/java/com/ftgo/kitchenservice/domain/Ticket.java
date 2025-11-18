package com.ftgo.kitchenservice.domain;

import com.ftgo.common.events.DomainEvent;
import com.ftgo.common.events.TicketAcceptedEvent;
import com.ftgo.common.events.TicketCreatedEvent;
import com.ftgo.common.events.TicketPreparingEvent;
import com.ftgo.common.events.TicketReadyEvent;
import com.ftgo.common.exception.InvalidOperationException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "tickets")
@Getter
@NoArgsConstructor
public class Ticket {
    @Id
    private String id;

    @Column(nullable = false)
    private String orderId;

    @Column(nullable = false)
    private String restaurantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketState state;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TicketLineItem> lineItems = new ArrayList<>();

    @Column(nullable = false)
    private String readyBy;

    @Column(nullable = false)
    private Instant createdAt;

    @Transient
    private List<DomainEvent> domainEvents = new ArrayList<>();

    public Ticket(String orderId, String restaurantId, List<TicketLineItem> lineItems, String readyBy) {
        this.id = UUID.randomUUID().toString();
        this.orderId = orderId;
        this.restaurantId = restaurantId;
        this.state = TicketState.CREATE_PENDING;
        this.readyBy = readyBy;
        this.createdAt = Instant.now();
        
        this.lineItems = lineItems;
        this.lineItems.forEach(item -> item.setTicket(this));
        
        this.domainEvents.add(new TicketCreatedEvent(
                this.id,
                this.orderId,
                this.restaurantId,
                this.lineItems.stream().map(item -> {
                    TicketCreatedEvent.TicketLineItem eventItem = new TicketCreatedEvent.TicketLineItem();
                    eventItem.setMenuItemId(item.getMenuItemId());
                    eventItem.setName(item.getName());
                    eventItem.setQuantity(item.getQuantity());
                    return eventItem;
                }).collect(Collectors.toList()),
                this.readyBy
        ));
    }

    public void accept(String readyBy) {
        if (this.state != TicketState.AWAITING_ACCEPTANCE) {
            throw new InvalidOperationException("Cannot accept ticket in state: " + this.state);
        }
        this.state = TicketState.ACCEPTED;
        this.readyBy = readyBy;
        this.domainEvents.add(new TicketAcceptedEvent(this.id, this.orderId, readyBy));
    }

    public void preparing() {
        if (this.state != TicketState.ACCEPTED) {
            throw new InvalidOperationException("Cannot mark as preparing in state: " + this.state);
        }
        this.state = TicketState.PREPARING;
        this.domainEvents.add(new TicketPreparingEvent(this.id, this.orderId));
    }

    public void ready() {
        if (this.state != TicketState.PREPARING) {
            throw new InvalidOperationException("Cannot mark as ready in state: " + this.state);
        }
        this.state = TicketState.READY_FOR_PICKUP;
        this.domainEvents.add(new TicketReadyEvent(this.id, this.orderId, this.readyBy));
    }

    public void confirmCreated() {
        if (this.state == TicketState.CREATE_PENDING) {
            this.state = TicketState.AWAITING_ACCEPTANCE;
        }
    }

    public List<DomainEvent> getDomainEvents() {
        return new ArrayList<>(domainEvents);
    }

    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
}

