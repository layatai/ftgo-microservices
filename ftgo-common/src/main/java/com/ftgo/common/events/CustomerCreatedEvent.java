package com.ftgo.common.events;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerCreatedEvent extends DomainEvent {
    private String customerId;
    private String name;
    private String email;

    public CustomerCreatedEvent() {
        super(null, "Customer");
    }

    public CustomerCreatedEvent(String customerId, String name, String email) {
        super(customerId, "Customer");
        this.customerId = customerId;
        this.name = name;
        this.email = email;
    }
}

