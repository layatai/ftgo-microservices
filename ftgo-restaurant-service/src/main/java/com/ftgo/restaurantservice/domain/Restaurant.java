package com.ftgo.restaurantservice.domain;

import com.ftgo.common.domain.Money;
import com.ftgo.common.events.DomainEvent;
import com.ftgo.common.events.MenuUpdatedEvent;
import com.ftgo.common.events.RestaurantCreatedEvent;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "restaurants")
@Getter
@NoArgsConstructor
public class Restaurant {
    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<MenuItem> menuItems = new ArrayList<>();

    @Transient
    private List<DomainEvent> domainEvents = new ArrayList<>();

    public Restaurant(String name, String address) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.address = address;
        this.domainEvents.add(new RestaurantCreatedEvent(this.id, this.name, this.address));
    }

    public void updateMenu(List<MenuItem> newMenuItems) {
        this.menuItems.clear();
        this.menuItems.addAll(newMenuItems);
        newMenuItems.forEach(item -> item.setRestaurant(this));
        
        List<MenuUpdatedEvent.MenuItemInfo> menuItemInfos = newMenuItems.stream()
                .map(item -> {
                    MenuUpdatedEvent.MenuItemInfo info = new MenuUpdatedEvent.MenuItemInfo();
                    info.setMenuItemId(item.getId());
                    info.setName(item.getName());
                    info.setPrice(item.getPrice().getAmount().toString());
                    info.setCurrency(item.getPrice().getCurrency());
                    return info;
                })
                .collect(Collectors.toList());
        
        this.domainEvents.add(new MenuUpdatedEvent(this.id, menuItemInfos));
    }

    public MenuItem findMenuItem(String menuItemId) {
        return menuItems.stream()
                .filter(item -> item.getId().equals(menuItemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("MenuItem not found: " + menuItemId));
    }

    public List<DomainEvent> getDomainEvents() {
        return new ArrayList<>(domainEvents);
    }

    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
}

