package com.ftgo.restaurantservice.domain;

import com.ftgo.common.domain.Money;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "menu_items")
@Getter
@NoArgsConstructor
public class MenuItem {
    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    @Setter
    private Restaurant restaurant;

    @Column(nullable = false)
    private String name;

    @Embedded
    private Money price;

    public MenuItem(String name, Money price) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.price = price;
    }
}

