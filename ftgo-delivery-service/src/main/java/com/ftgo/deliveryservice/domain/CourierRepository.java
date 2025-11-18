package com.ftgo.deliveryservice.domain;

import java.util.Optional;

public interface CourierRepository {
    Courier save(Courier courier);
    Optional<Courier> findById(String id);
}

