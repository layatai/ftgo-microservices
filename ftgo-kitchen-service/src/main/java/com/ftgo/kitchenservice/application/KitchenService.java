package com.ftgo.kitchenservice.application;

import com.ftgo.common.events.DomainEvent;
import com.ftgo.common.exception.EntityNotFoundException;
import com.ftgo.kitchenservice.domain.Ticket;
import com.ftgo.kitchenservice.domain.TicketLineItem;
import com.ftgo.kitchenservice.domain.TicketRepository;
import com.ftgo.kitchenservice.infrastructure.KitchenEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class KitchenService {
    private final TicketRepository ticketRepository;
    private final KitchenEventPublisher eventPublisher;

    @Transactional
    public Ticket createTicket(String orderId, String restaurantId, List<CreateTicketLineItemDTO> lineItemDTOs, String readyBy) {
        log.info("Creating ticket for order: {}", orderId);
        
        List<TicketLineItem> lineItems = lineItemDTOs.stream()
                .map(dto -> new TicketLineItem(dto.getMenuItemId(), dto.getName(), dto.getQuantity()))
                .collect(Collectors.toList());
        
        Ticket ticket = new Ticket(orderId, restaurantId, lineItems, readyBy);
        ticket = ticketRepository.save(ticket);
        ticket.confirmCreated();
        ticket = ticketRepository.save(ticket);
        
        publishDomainEvents(ticket);
        
        log.info("Created ticket with id: {}", ticket.getId());
        return ticket;
    }

    @Transactional(readOnly = true)
    public Ticket getTicket(String ticketId) {
        log.info("Getting ticket: {}", ticketId);
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with id: " + ticketId));
    }

    @Transactional
    public Ticket acceptTicket(String ticketId, String readyBy) {
        log.info("Accepting ticket: {}", ticketId);
        Ticket ticket = getTicket(ticketId);
        ticket.accept(readyBy);
        ticket = ticketRepository.save(ticket);
        publishDomainEvents(ticket);
        log.info("Accepted ticket: {}", ticketId);
        return ticket;
    }

    @Transactional
    public Ticket markPreparing(String ticketId) {
        log.info("Marking ticket as preparing: {}", ticketId);
        Ticket ticket = getTicket(ticketId);
        ticket.preparing();
        ticket = ticketRepository.save(ticket);
        publishDomainEvents(ticket);
        log.info("Marked ticket as preparing: {}", ticketId);
        return ticket;
    }

    @Transactional
    public Ticket markReady(String ticketId) {
        log.info("Marking ticket as ready: {}", ticketId);
        Ticket ticket = getTicket(ticketId);
        ticket.ready();
        ticket = ticketRepository.save(ticket);
        publishDomainEvents(ticket);
        log.info("Marked ticket as ready: {}", ticketId);
        return ticket;
    }

    private void publishDomainEvents(Ticket ticket) {
        List<DomainEvent> events = ticket.getDomainEvents();
        events.forEach(eventPublisher::publish);
        ticket.clearDomainEvents();
    }

    public record CreateTicketLineItemDTO(String menuItemId, String name, int quantity) {}
}

