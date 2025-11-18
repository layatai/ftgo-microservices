package com.ftgo.accountingservice.infrastructure;

import com.ftgo.accountingservice.domain.Invoice;
import com.ftgo.accountingservice.domain.InvoiceRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaInvoiceRepository extends JpaRepository<Invoice, String>, InvoiceRepository {
}

