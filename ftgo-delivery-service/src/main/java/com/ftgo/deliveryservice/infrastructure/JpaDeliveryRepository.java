package com.ftgo.deliveryservice.infrastructure;

import com.ftgo.deliveryservice.domain.Delivery;
import com.ftgo.deliveryservice.domain.DeliveryRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaDeliveryRepository extends JpaRepository<Delivery, String>, DeliveryRepository {
}

