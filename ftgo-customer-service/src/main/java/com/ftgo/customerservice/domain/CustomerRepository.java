package com.ftgo.customerservice.domain;

import java.util.Optional;

public interface CustomerRepository {
    Customer save(Customer customer);
    Optional<Customer> findById(String id);
    Optional<Customer> findByEmail(String email);
    boolean existsByEmail(String email);
}

