package com.ftgo.accountingservice.application.mapper;

import com.ftgo.common.domain.Money;
import com.ftgo.accountingservice.application.dto.InvoiceDTO;
import com.ftgo.accountingservice.application.dto.PaymentDTO;
import com.ftgo.accountingservice.domain.Invoice;
import com.ftgo.accountingservice.domain.Payment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountingMapper {
    InvoiceDTO toDTO(Invoice invoice);
    PaymentDTO toDTO(Payment payment);
    
    default String mapMoney(Money money) {
        return money != null ? money.getAmount().toString() : null;
    }
    
    default String mapCurrency(Money money) {
        return money != null ? money.getCurrency() : null;
    }
}

