package com.ftgo.customerservice.domain;

import com.ftgo.common.domain.Address;
import com.ftgo.common.events.CustomerCreatedEvent;
import com.ftgo.common.events.DomainEvent;
import com.ftgo.common.events.PaymentMethodAddedEvent;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "customers")
@Getter
@NoArgsConstructor
public class Customer {
    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Embedded
    private Address address;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PaymentMethod> paymentMethods = new ArrayList<>();

    @Transient
    private List<DomainEvent> domainEvents = new ArrayList<>();

    public Customer(String name, String email, Address address) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.email = email;
        this.address = address;
        this.domainEvents.add(new CustomerCreatedEvent(this.id, this.name, this.email));
    }

    public void addPaymentMethod(String paymentToken) {
        PaymentMethod paymentMethod = new PaymentMethod(this, paymentToken);
        this.paymentMethods.add(paymentMethod);
        this.domainEvents.add(new PaymentMethodAddedEvent(this.id, paymentMethod.getId(), paymentToken));
    }

    public List<DomainEvent> getDomainEvents() {
        return new ArrayList<>(domainEvents);
    }

    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
}

