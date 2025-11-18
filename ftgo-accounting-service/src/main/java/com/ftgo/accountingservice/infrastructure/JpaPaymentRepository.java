package com.ftgo.accountingservice.infrastructure;

import com.ftgo.accountingservice.domain.Payment;
import com.ftgo.accountingservice.domain.PaymentRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaPaymentRepository extends JpaRepository<Payment, String>, PaymentRepository {
}

