package com.bank.interfaces.controllers;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bank.application.usecases.GetTransactionUseCase;
import com.bank.application.usecases.ListTransactionsUseCase;
import com.bank.domain.entities.TransactionStatus;
import com.bank.interfaces.dtos.TransactionResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/transactions")
@Tag(name = "Transactions", description = "Transaction history queries")
@SecurityRequirement(name = "basicAuth")
public class TransactionController {

    private final ListTransactionsUseCase listTransactionsUseCase;
    private final GetTransactionUseCase getTransactionUseCase;

    public TransactionController(ListTransactionsUseCase listTransactionsUseCase,
                                  GetTransactionUseCase getTransactionUseCase) {
        this.listTransactionsUseCase = listTransactionsUseCase;
        this.getTransactionUseCase = getTransactionUseCase;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ANALYST','TELLER','SALES','COMPANY_SUPERVISOR','COMPANY_EMPLOYEE','NATURAL_CLIENT','BUSINESS_CLIENT')")
    @Operation(summary = "List transactions",
               description = "Lists transactions. Optionally filter by status. Clients only see their own transactions.")
    public List<TransactionResponse> list(@RequestParam(required = false) TransactionStatus status) {
        List<?> transactions = (status != null)
                ? getTransactionUseCase.findByStatus(status)
                : listTransactionsUseCase.execute();

        return transactions.stream()
                .map(obj -> {
                    var tx = (com.bank.domain.entities.Transaction) obj;
                    return new TransactionResponse(
                            tx.getId(),
                            tx.getTransactionType(),
                            tx.getAmount().value(),
                            tx.getDate(),
                            tx.getSourceAccount(),
                            tx.getDestinationAccount(),
                            tx.getStatus());
                })
                .toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ANALYST','TELLER','SALES','COMPANY_SUPERVISOR','COMPANY_EMPLOYEE','NATURAL_CLIENT','BUSINESS_CLIENT')")
    @Operation(summary = "Get transaction by ID",
               description = "Returns a specific transaction. Clients can only access transactions involving their accounts.")
    public TransactionResponse getById(@PathVariable String id) {
        var tx = getTransactionUseCase.findById(id);
        return new TransactionResponse(
                tx.getId(),
                tx.getTransactionType(),
                tx.getAmount().value(),
                tx.getDate(),
                tx.getSourceAccount(),
                tx.getDestinationAccount(),
                tx.getStatus());
    }
}
