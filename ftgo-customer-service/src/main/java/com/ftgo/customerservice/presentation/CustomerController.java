package com.ftgo.customerservice.presentation;

import com.ftgo.common.domain.Address;
import com.ftgo.common.exception.EntityNotFoundException;
import com.ftgo.customerservice.application.CustomerService;
import com.ftgo.customerservice.application.dto.AddPaymentMethodRequest;
import com.ftgo.customerservice.application.dto.CreateCustomerRequest;
import com.ftgo.customerservice.application.dto.CustomerDTO;
import com.ftgo.customerservice.application.mapper.CustomerMapper;
import com.ftgo.customerservice.domain.Customer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Customer", description = "Customer management APIs")
public class CustomerController {
    private final CustomerService customerService;
    private final CustomerMapper customerMapper;

    @PostMapping
    @Operation(summary = "Register a new customer")
    public ResponseEntity<CustomerDTO> createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        log.info("Creating customer: {}", request.getEmail());
        Customer customer = customerService.createCustomer(
                request.getName(),
                request.getEmail(),
                request.getAddress() != null ? request.getAddress() : new Address("", "", "", "")
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(customerMapper.toDTO(customer));
    }

    @GetMapping("/{customerId}")
    @Operation(summary = "Get customer details")
    public ResponseEntity<CustomerDTO> getCustomer(@PathVariable String customerId) {
        log.info("Getting customer: {}", customerId);
        Customer customer = customerService.getCustomer(customerId);
        return ResponseEntity.ok(customerMapper.toDTO(customer));
    }

    @PostMapping("/{customerId}/payment-methods")
    @Operation(summary = "Add payment method to customer")
    public ResponseEntity<CustomerDTO> addPaymentMethod(
            @PathVariable String customerId,
            @Valid @RequestBody AddPaymentMethodRequest request) {
        log.info("Adding payment method for customer: {}", customerId);
        Customer customer = customerService.addPaymentMethod(customerId, request.getPaymentToken());
        return ResponseEntity.ok(customerMapper.toDTO(customer));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}

