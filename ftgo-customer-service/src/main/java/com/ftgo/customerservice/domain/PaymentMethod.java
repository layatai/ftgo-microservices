package com.ftgo.customerservice.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "payment_methods")
@Getter
@NoArgsConstructor
public class PaymentMethod {
    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false)
    private String paymentToken;

    @Column(nullable = false)
    private boolean active = true;

    public PaymentMethod(Customer customer, String paymentToken) {
        this.id = UUID.randomUUID().toString();
        this.customer = customer;
        this.paymentToken = paymentToken;
    }

    public void deactivate() {
        this.active = false;
    }
}

