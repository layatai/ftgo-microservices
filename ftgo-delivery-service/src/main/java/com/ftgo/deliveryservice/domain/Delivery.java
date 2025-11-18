package com.ftgo.deliveryservice.domain;

import com.ftgo.common.events.DeliveryCreatedEvent;
import com.ftgo.common.events.DeliveryDeliveredEvent;
import com.ftgo.common.events.DeliveryPickedUpEvent;
import com.ftgo.common.events.DomainEvent;
import com.ftgo.common.exception.InvalidOperationException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "deliveries")
@Getter
@NoArgsConstructor
public class Delivery {
    @Id
    private String id;

    @Column(nullable = false)
    private String orderId;

    @Column(nullable = false)
    private String courierId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryState state;

    @Column(nullable = false)
    private String pickupAddress;

    @Column(nullable = false)
    private String deliveryAddress;

    @Column(nullable = false)
    private String pickupTime;

    private String pickedUpAt;
    private String deliveredAt;

    @Column(nullable = false)
    private Instant createdAt;

    @Transient
    private List<DomainEvent> domainEvents = new ArrayList<>();

    public Delivery(String orderId, String courierId, String pickupAddress, String deliveryAddress, String pickupTime) {
        this.id = UUID.randomUUID().toString();
        this.orderId = orderId;
        this.courierId = courierId;
        this.state = DeliveryState.PENDING;
        this.pickupAddress = pickupAddress;
        this.deliveryAddress = deliveryAddress;
        this.pickupTime = pickupTime;
        this.createdAt = Instant.now();
        
        this.domainEvents.add(new DeliveryCreatedEvent(
                this.id, this.orderId, this.courierId,
                this.pickupAddress, this.deliveryAddress, this.pickupTime
        ));
    }

    public void markPickedUp(String pickedUpAt) {
        if (this.state != DeliveryState.SCHEDULED && this.state != DeliveryState.PENDING) {
            throw new InvalidOperationException("Cannot mark as picked up in state: " + this.state);
        }
        this.state = DeliveryState.PICKED_UP;
        this.pickedUpAt = pickedUpAt;
        this.domainEvents.add(new DeliveryPickedUpEvent(this.id, this.orderId, pickedUpAt));
    }

    public void markDelivered(String deliveredAt) {
        if (this.state != DeliveryState.PICKED_UP) {
            throw new InvalidOperationException("Cannot mark as delivered in state: " + this.state);
        }
        this.state = DeliveryState.DELIVERED;
        this.deliveredAt = deliveredAt;
        this.domainEvents.add(new DeliveryDeliveredEvent(this.id, this.orderId, deliveredAt));
    }

    public void schedule() {
        if (this.state == DeliveryState.PENDING) {
            this.state = DeliveryState.SCHEDULED;
        }
    }

    public List<DomainEvent> getDomainEvents() {
        return new ArrayList<>(domainEvents);
    }

    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
}

