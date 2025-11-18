package com.ftgo.accountingservice.presentation;

import com.ftgo.common.domain.Money;
import com.ftgo.common.exception.EntityNotFoundException;
import com.ftgo.accountingservice.application.AccountingService;
import com.ftgo.accountingservice.application.dto.InvoiceDTO;
import com.ftgo.accountingservice.application.dto.PaymentDTO;
import com.ftgo.accountingservice.application.dto.ProcessPaymentRequest;
import com.ftgo.accountingservice.application.mapper.AccountingMapper;
import com.ftgo.accountingservice.domain.Invoice;
import com.ftgo.accountingservice.domain.Payment;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Accounting", description = "Accounting and payment APIs")
public class AccountingController {
    private final AccountingService accountingService;
    private final AccountingMapper accountingMapper;

    @GetMapping("/invoices/{invoiceId}")
    @Operation(summary = "Get invoice details")
    public ResponseEntity<InvoiceDTO> getInvoice(@PathVariable String invoiceId) {
        log.info("Getting invoice: {}", invoiceId);
        Invoice invoice = accountingService.getInvoice(invoiceId);
        return ResponseEntity.ok(accountingMapper.toDTO(invoice));
    }

    @PostMapping("/payments")
    @Operation(summary = "Process a payment")
    public ResponseEntity<PaymentDTO> processPayment(@Valid @RequestBody ProcessPaymentRequest request) {
        log.info("Processing payment for order: {}", request.getOrderId());
        Money amount = Money.of(request.getAmount(), request.getCurrency());
        Payment payment = accountingService.processPayment(
                request.getOrderId(),
                request.getCustomerId(),
                amount,
                request.getPaymentToken()
        );
        return ResponseEntity.ok(accountingMapper.toDTO(payment));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(404).body(ex.getMessage());
    }
}

