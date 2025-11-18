package com.ftgo.common.events;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.Instant;
import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    // Customer Events
    @JsonSubTypes.Type(value = CustomerCreatedEvent.class, name = "CustomerCreated"),
    @JsonSubTypes.Type(value = PaymentMethodAddedEvent.class, name = "PaymentMethodAdded"),
    
    // Restaurant Events
    @JsonSubTypes.Type(value = RestaurantCreatedEvent.class, name = "RestaurantCreated"),
    @JsonSubTypes.Type(value = MenuUpdatedEvent.class, name = "MenuUpdated"),
    
    // Order Events
    @JsonSubTypes.Type(value = OrderCreatedEvent.class, name = "OrderCreated"),
    @JsonSubTypes.Type(value = OrderApprovedEvent.class, name = "OrderApproved"),
    @JsonSubTypes.Type(value = OrderRejectedEvent.class, name = "OrderRejected"),
    @JsonSubTypes.Type(value = OrderCancelledEvent.class, name = "OrderCancelled"),
    
    // Kitchen Events
    @JsonSubTypes.Type(value = TicketCreatedEvent.class, name = "TicketCreated"),
    @JsonSubTypes.Type(value = TicketAcceptedEvent.class, name = "TicketAccepted"),
    @JsonSubTypes.Type(value = TicketPreparingEvent.class, name = "TicketPreparing"),
    @JsonSubTypes.Type(value = TicketReadyEvent.class, name = "TicketReady"),
    
    // Delivery Events
    @JsonSubTypes.Type(value = DeliveryCreatedEvent.class, name = "DeliveryCreated"),
    @JsonSubTypes.Type(value = DeliveryPickedUpEvent.class, name = "DeliveryPickedUp"),
    @JsonSubTypes.Type(value = DeliveryDeliveredEvent.class, name = "DeliveryDelivered")
})
public abstract class DomainEvent {
    private final String eventId;
    private final Instant occurredAt;
    private final String aggregateId;
    private final String aggregateType;

    protected DomainEvent(String aggregateId, String aggregateType) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredAt = Instant.now();
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
    }

    public String getEventId() {
        return eventId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public String getAggregateType() {
        return aggregateType;
    }
}

