package com.ftgo.orderservice.application;

import com.ftgo.common.domain.Money;
import com.ftgo.common.events.DomainEvent;
import com.ftgo.common.exception.EntityNotFoundException;
import com.ftgo.common.exception.InvalidOperationException;
import com.ftgo.orderservice.domain.Order;
import com.ftgo.orderservice.domain.OrderLineItem;
import com.ftgo.orderservice.domain.OrderRepository;
import com.ftgo.orderservice.domain.OrderState;
import com.ftgo.orderservice.infrastructure.OrderEventPublisher;
import com.ftgo.orderservice.infrastructure.RestaurantServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderEventPublisher eventPublisher;
    private final RestaurantServiceClient restaurantServiceClient;

    @Transactional
    public Order createOrder(String customerId, String restaurantId, List<CreateOrderLineItemDTO> lineItemDTOs,
                           String deliveryAddress, String deliveryTime) {
        log.info("Creating order for customer: {} at restaurant: {}", customerId, restaurantId);
        
        // Validate menu items and get prices
        List<OrderLineItem> lineItems = lineItemDTOs.stream()
                .map(dto -> {
                    // In a real implementation, we would fetch menu item details from restaurant service
                    // For now, we'll use the provided price
                    Money price = Money.of(dto.getPrice(), dto.getCurrency());
                    return new OrderLineItem(dto.getMenuItemId(), dto.getName(), dto.getQuantity(), price);
                })
                .collect(Collectors.toList());
        
        Order order = new Order(customerId, restaurantId, lineItems, deliveryAddress, deliveryTime);
        order = orderRepository.save(order);
        
        publishDomainEvents(order);
        
        log.info("Created order with id: {}", order.getId());
        return order;
    }

    @Transactional(readOnly = true)
    public Order getOrder(String orderId) {
        log.info("Getting order: {}", orderId);
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));
    }

    @Transactional
    public Order approveOrder(String orderId) {
        log.info("Approving order: {}", orderId);
        Order order = getOrder(orderId);
        order.approve();
        order = orderRepository.save(order);
        publishDomainEvents(order);
        log.info("Approved order: {}", orderId);
        return order;
    }

    @Transactional
    public Order rejectOrder(String orderId, String reason) {
        log.info("Rejecting order: {} with reason: {}", orderId, reason);
        Order order = getOrder(orderId);
        order.reject(reason);
        order = orderRepository.save(order);
        publishDomainEvents(order);
        log.info("Rejected order: {}", orderId);
        return order;
    }

    @Transactional
    public Order cancelOrder(String orderId, String reason) {
        log.info("Cancelling order: {} with reason: {}", orderId, reason);
        Order order = getOrder(orderId);
        order.cancel(reason);
        order = orderRepository.save(order);
        publishDomainEvents(order);
        log.info("Cancelled order: {}", orderId);
        return order;
    }

    private void publishDomainEvents(Order order) {
        List<DomainEvent> events = order.getDomainEvents();
        events.forEach(eventPublisher::publish);
        order.clearDomainEvents();
    }

    public record CreateOrderLineItemDTO(String menuItemId, String name, int quantity, String price, String currency) {}
}

