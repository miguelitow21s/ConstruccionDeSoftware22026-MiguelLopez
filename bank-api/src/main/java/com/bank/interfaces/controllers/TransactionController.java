package com.bank.interfaces.controllers;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bank.application.usecases.ListTransactionsUseCase;
import com.bank.interfaces.dtos.TransactionResponse;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/transactions")
@Tag(name = "Transactions", description = "Transaction history queries")
@SecurityRequirement(name = "basicAuth")
public class TransactionController {

    private final ListTransactionsUseCase listTransactionsUseCase;

    public TransactionController(ListTransactionsUseCase listTransactionsUseCase) {
        this.listTransactionsUseCase = listTransactionsUseCase;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ANALYST','TELLER','SALES','COMPANY_SUPERVISOR','COMPANY_EMPLOYEE','NATURAL_CLIENT','BUSINESS_CLIENT')")
    public List<TransactionResponse> list() {
        return listTransactionsUseCase.execute().stream()
                .map(tx -> new TransactionResponse(
                        tx.getId(),
                        tx.getTransactionType(),
                        tx.getAmount().value(),
                        tx.getDate(),
                        tx.getSourceAccount(),
                        tx.getDestinationAccount(),
                        tx.getStatus()))
                .toList();
    }
}
