package com.ftgo.customerservice.application.mapper;

import com.ftgo.customerservice.application.dto.CustomerDTO;
import com.ftgo.customerservice.application.dto.PaymentMethodDTO;
import com.ftgo.customerservice.domain.Customer;
import com.ftgo.customerservice.domain.PaymentMethod;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    CustomerDTO toDTO(Customer customer);
    
    PaymentMethodDTO toDTO(PaymentMethod paymentMethod);
    
    List<PaymentMethodDTO> toPaymentMethodDTOs(List<PaymentMethod> paymentMethods);
}

