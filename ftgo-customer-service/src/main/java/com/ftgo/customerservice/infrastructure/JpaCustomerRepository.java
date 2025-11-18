package com.ftgo.customerservice.infrastructure;

import com.ftgo.customerservice.domain.Customer;
import com.ftgo.customerservice.domain.CustomerRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaCustomerRepository extends JpaRepository<Customer, String>, CustomerRepository {
    @Override
    Optional<Customer> findByEmail(String email);
    
    @Override
    boolean existsByEmail(String email);
}

