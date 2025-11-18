package com.ftgo.orderservice.saga;

import com.ftgo.orderservice.saga.steps.AuthorizeCardStep;
import com.ftgo.orderservice.saga.steps.ConfirmCreateOrderStep;
import com.ftgo.orderservice.saga.steps.CreateTicketStep;
import com.ftgo.orderservice.saga.steps.ValidateOrderStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Defines the Create Order Saga as described in Chapter 4.
 * This saga orchestrates the order creation process across multiple services.
 * 
 * Uses orchestration-based pattern where SagaManager coordinates all steps.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CreateOrderSagaDefinition implements SagaDefinition {
    private final ValidateOrderStep validateOrderStep;
    private final CreateTicketStep createTicketStep;
    private final AuthorizeCardStep authorizeCardStep;
    private final ConfirmCreateOrderStep confirmCreateOrderStep;

    @Override
    public List<AsyncSagaStep> getSteps() {
        return Arrays.asList(
                validateOrderStep,
                createTicketStep,
                authorizeCardStep,
                confirmCreateOrderStep
        );
    }

    @Override
    public String getSagaType() {
        return "CreateOrderSaga";
    }
}
