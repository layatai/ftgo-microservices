package com.ftgo.kitchenservice.domain;

import java.util.Optional;

public interface TicketRepository {
    Ticket save(Ticket ticket);
    Optional<Ticket> findById(String id);
    Optional<Ticket> findByOrderId(String orderId);
}

