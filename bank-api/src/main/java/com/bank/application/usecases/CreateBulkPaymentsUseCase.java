package com.bank.application.usecases;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.Transaction;

@Service
public class CreateBulkPaymentsUseCase {

    private final TransferMoneyUseCase transferMoneyUseCase;
    private final AuthContextService authContextService;

    public CreateBulkPaymentsUseCase(TransferMoneyUseCase transferMoneyUseCase,
                                    AuthContextService authContextService) {
        this.transferMoneyUseCase = transferMoneyUseCase;
        this.authContextService = authContextService;
    }

    public List<Transaction> execute(String sourceAccountId, List<BulkPaymentItem> payments) {
        if (!authContextService.hasRole("COMPANY_EMPLOYEE")) {
            throw new SecurityException("Not authorized to create payments bulk");
        }
        if (payments == null || payments.isEmpty()) {
            throw new IllegalArgumentException("Bulk payments list cannot be empty");
        }

        return payments.stream()
                .map(payment -> transferMoneyUseCase.execute(
                        sourceAccountId,
                        payment.destinationAccountId(),
                        payment.amount(),
                        true
                ))
                .toList();
    }

    public record BulkPaymentItem(String destinationAccountId, BigDecimal amount) {
    }
}
