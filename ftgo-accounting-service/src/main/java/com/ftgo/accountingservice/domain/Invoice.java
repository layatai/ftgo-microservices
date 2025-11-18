package com.ftgo.accountingservice.domain;

import com.ftgo.common.domain.Money;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "invoices")
@Getter
@NoArgsConstructor
public class Invoice {
    @Id
    private String id;

    @Column(nullable = false)
    private String orderId;

    @Column(nullable = false)
    private String customerId;

    @Embedded
    private Money amount;

    @Column(nullable = false)
    private Instant createdAt;

    public Invoice(String orderId, String customerId, Money amount) {
        this.id = UUID.randomUUID().toString();
        this.orderId = orderId;
        this.customerId = customerId;
        this.amount = amount;
        this.createdAt = Instant.now();
    }
}

