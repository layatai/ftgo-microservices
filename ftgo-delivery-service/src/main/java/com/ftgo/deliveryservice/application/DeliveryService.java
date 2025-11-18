package com.ftgo.deliveryservice.application;

import com.ftgo.common.events.DomainEvent;
import com.ftgo.common.exception.EntityNotFoundException;
import com.ftgo.deliveryservice.domain.Courier;
import com.ftgo.deliveryservice.domain.Delivery;
import com.ftgo.deliveryservice.domain.CourierRepository;
import com.ftgo.deliveryservice.domain.DeliveryRepository;
import com.ftgo.deliveryservice.infrastructure.DeliveryEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryService {
    private final DeliveryRepository deliveryRepository;
    private final CourierRepository courierRepository;
    private final DeliveryEventPublisher eventPublisher;

    @Transactional
    public Courier registerCourier(String name, String phoneNumber) {
        log.info("Registering courier: {}", name);
        Courier courier = new Courier(name, phoneNumber);
        courier = courierRepository.save(courier);
        log.info("Registered courier with id: {}", courier.getId());
        return courier;
    }

    @Transactional(readOnly = true)
    public Courier getCourier(String courierId) {
        return courierRepository.findById(courierId)
                .orElseThrow(() -> new EntityNotFoundException("Courier not found with id: " + courierId));
    }

    @Transactional
    public Delivery createDelivery(String orderId, String courierId, String pickupAddress, 
                                  String deliveryAddress, String pickupTime) {
        log.info("Creating delivery for order: {}", orderId);
        Delivery delivery = new Delivery(orderId, courierId, pickupAddress, deliveryAddress, pickupTime);
        delivery.schedule();
        delivery = deliveryRepository.save(delivery);
        publishDomainEvents(delivery);
        log.info("Created delivery with id: {}", delivery.getId());
        return delivery;
    }

    @Transactional(readOnly = true)
    public Delivery getDelivery(String deliveryId) {
        return deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new EntityNotFoundException("Delivery not found with id: " + deliveryId));
    }

    @Transactional
    public Delivery markPickedUp(String deliveryId, String pickedUpAt) {
        log.info("Marking delivery as picked up: {}", deliveryId);
        Delivery delivery = getDelivery(deliveryId);
        delivery.markPickedUp(pickedUpAt);
        delivery = deliveryRepository.save(delivery);
        publishDomainEvents(delivery);
        return delivery;
    }

    @Transactional
    public Delivery markDelivered(String deliveryId, String deliveredAt) {
        log.info("Marking delivery as delivered: {}", deliveryId);
        Delivery delivery = getDelivery(deliveryId);
        delivery.markDelivered(deliveredAt);
        delivery = deliveryRepository.save(delivery);
        publishDomainEvents(delivery);
        return delivery;
    }

    private void publishDomainEvents(Delivery delivery) {
        List<DomainEvent> events = delivery.getDomainEvents();
        events.forEach(eventPublisher::publish);
        delivery.clearDomainEvents();
    }
}

