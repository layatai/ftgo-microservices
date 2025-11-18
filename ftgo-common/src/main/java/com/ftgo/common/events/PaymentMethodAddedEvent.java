package com.ftgo.common.events;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentMethodAddedEvent extends DomainEvent {
    private String customerId;
    private String paymentMethodId;
    private String paymentToken;

    public PaymentMethodAddedEvent(String customerId, String paymentMethodId, String paymentToken) {
        super(customerId, "Customer");
        this.customerId = customerId;
        this.paymentMethodId = paymentMethodId;
        this.paymentToken = paymentToken;
    }
}

