package com.ftgo.deliveryservice.infrastructure;

import com.ftgo.deliveryservice.domain.Courier;
import com.ftgo.deliveryservice.domain.CourierRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaCourierRepository extends JpaRepository<Courier, String>, CourierRepository {
}

