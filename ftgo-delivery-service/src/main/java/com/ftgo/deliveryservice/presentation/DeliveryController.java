package com.ftgo.deliveryservice.presentation;

import com.ftgo.common.exception.EntityNotFoundException;
import com.ftgo.deliveryservice.application.DeliveryService;
import com.ftgo.deliveryservice.application.dto.*;
import com.ftgo.deliveryservice.application.mapper.DeliveryMapper;
import com.ftgo.deliveryservice.domain.Courier;
import com.ftgo.deliveryservice.domain.Delivery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Delivery", description = "Delivery management APIs")
public class DeliveryController {
    private final DeliveryService deliveryService;
    private final DeliveryMapper deliveryMapper;

    @PostMapping("/couriers")
    @Operation(summary = "Register a new courier")
    public ResponseEntity<CourierDTO> registerCourier(@Valid @RequestBody CreateCourierRequest request) {
        log.info("Registering courier: {}", request.getName());
        Courier courier = deliveryService.registerCourier(request.getName(), request.getPhoneNumber());
        return ResponseEntity.status(HttpStatus.CREATED).body(deliveryMapper.toDTO(courier));
    }

    @PostMapping("/deliveries")
    @Operation(summary = "Create a new delivery")
    public ResponseEntity<DeliveryDTO> createDelivery(@Valid @RequestBody CreateDeliveryRequest request) {
        log.info("Creating delivery for order: {}", request.getOrderId());
        Delivery delivery = deliveryService.createDelivery(
                request.getOrderId(),
                request.getCourierId(),
                request.getPickupAddress(),
                request.getDeliveryAddress(),
                request.getPickupTime()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(deliveryMapper.toDTO(delivery));
    }

    @GetMapping("/deliveries/{deliveryId}")
    @Operation(summary = "Get delivery status")
    public ResponseEntity<DeliveryDTO> getDelivery(@PathVariable String deliveryId) {
        log.info("Getting delivery: {}", deliveryId);
        Delivery delivery = deliveryService.getDelivery(deliveryId);
        return ResponseEntity.ok(deliveryMapper.toDTO(delivery));
    }

    @PutMapping("/deliveries/{deliveryId}/pickup")
    @Operation(summary = "Mark delivery as picked up")
    public ResponseEntity<DeliveryDTO> markPickedUp(
            @PathVariable String deliveryId,
            @RequestParam String pickedUpAt) {
        log.info("Marking delivery as picked up: {}", deliveryId);
        Delivery delivery = deliveryService.markPickedUp(deliveryId, pickedUpAt);
        return ResponseEntity.ok(deliveryMapper.toDTO(delivery));
    }

    @PutMapping("/deliveries/{deliveryId}/delivered")
    @Operation(summary = "Mark delivery as delivered")
    public ResponseEntity<DeliveryDTO> markDelivered(
            @PathVariable String deliveryId,
            @RequestParam String deliveredAt) {
        log.info("Marking delivery as delivered: {}", deliveryId);
        Delivery delivery = deliveryService.markDelivered(deliveryId, deliveredAt);
        return ResponseEntity.ok(deliveryMapper.toDTO(delivery));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}

