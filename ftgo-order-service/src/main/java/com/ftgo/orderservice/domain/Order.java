package com.ftgo.orderservice.domain;

import com.ftgo.common.domain.Money;
import com.ftgo.common.events.DomainEvent;
import com.ftgo.common.events.OrderApprovedEvent;
import com.ftgo.common.events.OrderCancelledEvent;
import com.ftgo.common.events.OrderCreatedEvent;
import com.ftgo.common.events.OrderRejectedEvent;
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
@Table(name = "orders")
@Getter
@NoArgsConstructor
public class Order {
    @Id
    private String id;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false)
    private String restaurantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderState state;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderLineItem> lineItems = new ArrayList<>();

    @Embedded
    private Money orderTotal;

    @Column(nullable = false)
    private String deliveryAddress;

    @Column(nullable = false)
    private String deliveryTime;

    @Column(nullable = false)
    private Instant createdAt;

    @Transient
    private List<DomainEvent> domainEvents = new ArrayList<>();

    public Order(String customerId, String restaurantId, List<OrderLineItem> lineItems,
                 String deliveryAddress, String deliveryTime) {
        this.id = UUID.randomUUID().toString();
        this.customerId = customerId;
        this.restaurantId = restaurantId;
        this.state = OrderState.PENDING;
        this.deliveryAddress = deliveryAddress;
        this.deliveryTime = deliveryTime;
        this.createdAt = Instant.now();
        
        this.lineItems = lineItems;
        this.lineItems.forEach(item -> item.setOrder(this));
        
        this.orderTotal = calculateTotal();
        
        this.domainEvents.add(new OrderCreatedEvent(
                this.id,
                this.customerId,
                this.restaurantId,
                this.state.name(),
                this.lineItems.stream().map(item -> {
                    OrderCreatedEvent.OrderLineItem eventItem = new OrderCreatedEvent.OrderLineItem();
                    eventItem.setMenuItemId(item.getMenuItemId());
                    eventItem.setName(item.getName());
                    eventItem.setQuantity(item.getQuantity());
                    eventItem.setPrice(item.getPrice().getAmount().toString());
                    eventItem.setCurrency(item.getPrice().getCurrency());
                    return eventItem;
                }).collect(Collectors.toList()),
                this.deliveryAddress,
                this.deliveryTime
        ));
    }

    public void approve() {
        if (this.state != OrderState.PENDING) {
            throw new InvalidOperationException("Cannot approve order in state: " + this.state);
        }
        this.state = OrderState.APPROVED;
        this.domainEvents.add(new OrderApprovedEvent(this.id, this.customerId, this.restaurantId));
    }

    public void reject(String reason) {
        if (this.state != OrderState.PENDING) {
            throw new InvalidOperationException("Cannot reject order in state: " + this.state);
        }
        this.state = OrderState.REJECTED;
        this.domainEvents.add(new OrderRejectedEvent(this.id, reason));
    }

    public void cancel(String reason) {
        if (this.state == OrderState.CANCELLED || this.state == OrderState.REJECTED) {
            throw new InvalidOperationException("Cannot cancel order in state: " + this.state);
        }
        this.state = OrderState.CANCELLED;
        this.domainEvents.add(new OrderCancelledEvent(this.id, reason));
    }

    private Money calculateTotal() {
        return lineItems.stream()
                .map(item -> item.getPrice().multiply(item.getQuantity()))
                .reduce(Money.of("0", "USD"), Money::add);
    }

    public List<DomainEvent> getDomainEvents() {
        return new ArrayList<>(domainEvents);
    }

    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
}

