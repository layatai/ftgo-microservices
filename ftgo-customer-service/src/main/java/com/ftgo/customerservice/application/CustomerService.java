package com.ftgo.customerservice.application;

import com.ftgo.common.domain.Address;
import com.ftgo.common.events.DomainEvent;
import com.ftgo.common.exception.EntityNotFoundException;
import com.ftgo.common.exception.InvalidOperationException;
import com.ftgo.customerservice.domain.Customer;
import com.ftgo.customerservice.domain.CustomerRepository;
import com.ftgo.customerservice.infrastructure.CustomerEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final CustomerEventPublisher eventPublisher;

    @Transactional
    public Customer createCustomer(String name, String email, Address address) {
        log.info("Creating customer with email: {}", email);
        
        if (customerRepository.existsByEmail(email)) {
            throw new InvalidOperationException("Customer with email " + email + " already exists");
        }

        Customer customer = new Customer(name, email, address);
        customer = customerRepository.save(customer);
        
        publishDomainEvents(customer);
        
        log.info("Created customer with id: {}", customer.getId());
        return customer;
    }

    @Transactional(readOnly = true)
    public Customer getCustomer(String customerId) {
        log.info("Getting customer with id: {}", customerId);
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + customerId));
    }

    @Transactional(readOnly = true)
    public Customer getCustomerByEmail(String email) {
        log.info("Getting customer with email: {}", email);
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with email: " + email));
    }

    @Transactional
    public Customer addPaymentMethod(String customerId, String paymentToken) {
        log.info("Adding payment method for customer: {}", customerId);
        
        Customer customer = getCustomer(customerId);
        customer.addPaymentMethod(paymentToken);
        customer = customerRepository.save(customer);
        
        publishDomainEvents(customer);
        
        log.info("Added payment method for customer: {}", customerId);
        return customer;
    }

    private void publishDomainEvents(Customer customer) {
        List<DomainEvent> events = customer.getDomainEvents();
        events.forEach(eventPublisher::publish);
        customer.clearDomainEvents();
    }
}

