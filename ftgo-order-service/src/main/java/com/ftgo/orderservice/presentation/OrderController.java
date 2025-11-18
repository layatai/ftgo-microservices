package com.ftgo.orderservice.presentation;

import com.ftgo.common.exception.EntityNotFoundException;
import com.ftgo.common.exception.InvalidOperationException;
import com.ftgo.orderservice.application.OrderService;
import com.ftgo.orderservice.application.dto.CreateOrderLineItemRequest;
import com.ftgo.orderservice.application.dto.CreateOrderRequest;
import com.ftgo.orderservice.application.dto.OrderDTO;
import com.ftgo.orderservice.application.mapper.OrderMapper;
import com.ftgo.orderservice.domain.Order;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order", description = "Order management APIs")
public class OrderController {
    private final OrderService orderService;
    private final OrderMapper orderMapper;

    @PostMapping
    @Operation(summary = "Create a new order")
    public ResponseEntity<OrderDTO> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        log.info("Creating order for customer: {}", request.getCustomerId());
        
        var lineItemDTOs = request.getLineItems().stream()
                .map(item -> new OrderService.CreateOrderLineItemDTO(
                        item.getMenuItemId(),
                        item.getName(),
                        item.getQuantity(),
                        item.getPrice(),
                        item.getCurrency()
                ))
                .collect(Collectors.toList());
        
        // Default delivery time to "ASAP" if not provided
        String deliveryTime = request.getDeliveryTime();
        if (deliveryTime == null || deliveryTime.isBlank()) {
            deliveryTime = "ASAP";
        }
        
        Order order = orderService.createOrder(
                request.getCustomerId(),
                request.getRestaurantId(),
                lineItemDTOs,
                request.getDeliveryAddress(),
                deliveryTime,
                idempotencyKey
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(orderMapper.toDTO(order));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order details")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable String orderId) {
        log.info("Getting order: {}", orderId);
        Order order = orderService.getOrder(orderId);
        return ResponseEntity.ok(orderMapper.toDTO(order));
    }

    @PutMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel an order")
    public ResponseEntity<OrderDTO> cancelOrder(
            @PathVariable String orderId,
            @RequestParam(required = false, defaultValue = "Customer requested cancellation") String reason) {
        log.info("Cancelling order: {}", orderId);
        Order order = orderService.cancelOrder(orderId, reason);
        return ResponseEntity.ok(orderMapper.toDTO(order));
    }

    @ExceptionHandler({EntityNotFoundException.class, InvalidOperationException.class})
    public ResponseEntity<String> handleExceptions(RuntimeException ex) {
        if (ex instanceof EntityNotFoundException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}

