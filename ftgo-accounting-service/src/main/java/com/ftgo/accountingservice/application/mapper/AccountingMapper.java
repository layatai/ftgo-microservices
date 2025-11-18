package com.ftgo.accountingservice.application.mapper;

import com.ftgo.common.domain.Money;
import com.ftgo.accountingservice.application.dto.InvoiceDTO;
import com.ftgo.accountingservice.application.dto.PaymentDTO;
import com.ftgo.accountingservice.domain.Invoice;
import com.ftgo.accountingservice.domain.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountingMapper {
    @Mapping(target = "amount", source = "amount", qualifiedByName = "mapMoneyAmount")
    @Mapping(target = "currency", source = "amount", qualifiedByName = "mapMoneyCurrency")
    InvoiceDTO toDTO(Invoice invoice);
    
    @Mapping(target = "amount", source = "amount", qualifiedByName = "mapMoneyAmount")
    @Mapping(target = "currency", source = "amount", qualifiedByName = "mapMoneyCurrency")
    PaymentDTO toDTO(Payment payment);
    
    @org.mapstruct.Named("mapMoneyAmount")
    default String mapMoneyAmount(Money money) {
        return money != null ? money.getAmount().toString() : null;
    }
    
    @org.mapstruct.Named("mapMoneyCurrency")
    default String mapMoneyCurrency(Money money) {
        return money != null ? money.getCurrency() : null;
    }
}

