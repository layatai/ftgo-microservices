package com.ftgo.kitchenservice.infrastructure;

import com.ftgo.kitchenservice.domain.Ticket;
import com.ftgo.kitchenservice.domain.TicketRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaTicketRepository extends JpaRepository<Ticket, String>, TicketRepository {
    @Override
    Optional<Ticket> findByOrderId(String orderId);
}

