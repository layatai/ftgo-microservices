package com.ftgo.kitchenservice.presentation;

import com.ftgo.common.exception.EntityNotFoundException;
import com.ftgo.kitchenservice.application.KitchenService;
import com.ftgo.kitchenservice.application.dto.CreateTicketRequest;
import com.ftgo.kitchenservice.application.dto.TicketDTO;
import com.ftgo.kitchenservice.application.mapper.TicketMapper;
import com.ftgo.kitchenservice.domain.Ticket;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Kitchen", description = "Kitchen ticket management APIs")
public class KitchenController {
    private final KitchenService kitchenService;
    private final TicketMapper ticketMapper;

    @PostMapping
    @Operation(summary = "Create a new ticket")
    public ResponseEntity<TicketDTO> createTicket(@Valid @RequestBody CreateTicketRequest request) {
        log.info("Creating ticket for order: {}", request.getOrderId());
        
        var lineItemDTOs = request.getLineItems().stream()
                .map(item -> new KitchenService.CreateTicketLineItemDTO(
                        item.getMenuItemId(),
                        item.getName(),
                        item.getQuantity()
                ))
                .collect(Collectors.toList());
        
        Ticket ticket = kitchenService.createTicket(
                request.getOrderId(),
                request.getRestaurantId(),
                lineItemDTOs,
                request.getReadyBy()
        );
        
        return ResponseEntity.ok(ticketMapper.toDTO(ticket));
    }

    @GetMapping("/{ticketId}")
    @Operation(summary = "Get ticket status")
    public ResponseEntity<TicketDTO> getTicket(@PathVariable String ticketId) {
        log.info("Getting ticket: {}", ticketId);
        Ticket ticket = kitchenService.getTicket(ticketId);
        return ResponseEntity.ok(ticketMapper.toDTO(ticket));
    }

    @PutMapping("/{ticketId}/accept")
    @Operation(summary = "Accept a ticket")
    public ResponseEntity<TicketDTO> acceptTicket(
            @PathVariable String ticketId,
            @RequestParam String readyBy) {
        log.info("Accepting ticket: {}", ticketId);
        Ticket ticket = kitchenService.acceptTicket(ticketId, readyBy);
        return ResponseEntity.ok(ticketMapper.toDTO(ticket));
    }

    @PutMapping("/{ticketId}/preparing")
    @Operation(summary = "Mark ticket as preparing")
    public ResponseEntity<TicketDTO> markPreparing(@PathVariable String ticketId) {
        log.info("Marking ticket as preparing: {}", ticketId);
        Ticket ticket = kitchenService.markPreparing(ticketId);
        return ResponseEntity.ok(ticketMapper.toDTO(ticket));
    }

    @PutMapping("/{ticketId}/ready")
    @Operation(summary = "Mark ticket as ready")
    public ResponseEntity<TicketDTO> markReady(@PathVariable String ticketId) {
        log.info("Marking ticket as ready: {}", ticketId);
        Ticket ticket = kitchenService.markReady(ticketId);
        return ResponseEntity.ok(ticketMapper.toDTO(ticket));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(404).body(ex.getMessage());
    }
}

