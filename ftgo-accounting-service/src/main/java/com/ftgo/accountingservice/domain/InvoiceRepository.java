package com.ftgo.accountingservice.domain;

import java.util.Optional;

public interface InvoiceRepository {
    Invoice save(Invoice invoice);
    Optional<Invoice> findById(String id);
}

