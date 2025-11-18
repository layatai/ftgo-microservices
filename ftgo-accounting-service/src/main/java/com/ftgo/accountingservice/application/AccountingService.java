package com.ftgo.accountingservice.application;

import com.ftgo.common.domain.Money;
import com.ftgo.common.exception.EntityNotFoundException;
import com.ftgo.accountingservice.domain.Invoice;
import com.ftgo.accountingservice.domain.Payment;
import com.ftgo.accountingservice.domain.InvoiceRepository;
import com.ftgo.accountingservice.domain.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountingService {
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public Invoice getInvoice(String invoiceId) {
        log.info("Getting invoice: {}", invoiceId);
        return invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new EntityNotFoundException("Invoice not found with id: " + invoiceId));
    }

    @Transactional
    public Invoice createInvoice(String orderId, String customerId, Money amount) {
        log.info("Creating invoice for order: {}", orderId);
        Invoice invoice = new Invoice(orderId, customerId, amount);
        invoice = invoiceRepository.save(invoice);
        log.info("Created invoice with id: {}", invoice.getId());
        return invoice;
    }

    @Transactional
    public Payment processPayment(String orderId, String customerId, Money amount, String paymentToken) {
        log.info("Processing payment for order: {}", orderId);
        Payment payment = new Payment(orderId, customerId, amount, paymentToken);
        
        // Simulate payment processing
        try {
            // In a real implementation, this would call a payment gateway
            Thread.sleep(100); // Simulate processing time
            payment.complete();
        } catch (Exception e) {
            log.error("Payment processing failed", e);
            payment.fail();
        }
        
        payment = paymentRepository.save(payment);
        log.info("Processed payment with id: {}", payment.getId());
        return payment;
    }
}

