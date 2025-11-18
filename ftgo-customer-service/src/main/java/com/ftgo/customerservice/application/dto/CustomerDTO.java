package com.ftgo.customerservice.application.dto;

import com.ftgo.common.domain.Address;
import lombok.Data;

import java.util.List;

@Data
public class CustomerDTO {
    private String id;
    private String name;
    private String email;
    private Address address;
    private List<PaymentMethodDTO> paymentMethods;
}

