package com.ftgo.deliveryservice.domain;

import java.util.Optional;

public interface DeliveryRepository {
    Delivery save(Delivery delivery);
    Optional<Delivery> findById(String id);
}

