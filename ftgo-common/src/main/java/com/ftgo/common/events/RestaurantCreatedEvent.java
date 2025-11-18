package com.ftgo.common.events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RestaurantCreatedEvent extends DomainEvent {
    private String restaurantId;
    private String name;
    private String address;

    public RestaurantCreatedEvent(String restaurantId, String name, String address) {
        super(restaurantId, "Restaurant");
        this.restaurantId = restaurantId;
        this.name = name;
        this.address = address;
    }
}

