package com.ftgo.orderservice.infrastructure;

import com.ftgo.orderservice.domain.Order;
import com.ftgo.orderservice.domain.OrderRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaOrderRepository extends JpaRepository<Order, String>, OrderRepository {
}

