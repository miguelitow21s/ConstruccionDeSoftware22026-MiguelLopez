package com.bank.application.usecases;

import java.util.List;

import org.springframework.stereotype.Service;

import com.bank.application.ports.AccountRepositoryPort;
import com.bank.application.ports.TransactionRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.Transaction;

@Service
public class ListTransactionsUseCase {

    private final TransactionRepositoryPort transactionRepository;
    private final AccountRepositoryPort accountRepository;
    private final AuthContextService authContextService;

    public ListTransactionsUseCase(TransactionRepositoryPort transactionRepository,
                                      AccountRepositoryPort accountRepository,
                                      AuthContextService authContextService) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.authContextService = authContextService;
    }

    public List<Transaction> execute() {
        if (authContextService.hasRole("ANALYST") || authContextService.hasRole("TELLER")) {
            return transactionRepository.findAll();
        }

        if (authContextService.hasAnyRole("NATURAL_CLIENT", "BUSINESS_CLIENT", "COMPANY_EMPLOYEE", "COMPANY_SUPERVISOR", "SALES")) {
            String clientId = authContextService.currentRelatedClientIdOrThrow();
            List<String> accountsClient = accountRepository.findByClientId(clientId).stream()
                    .map(c -> c.getAccountNumber().value())
                    .toList();
            if (accountsClient.isEmpty()) {
                return List.of();
            }
            return transactionRepository.findByAccountSourceInOrAccountDestinationIn(accountsClient, accountsClient);
        }

        throw new SecurityException("Not authorized to get transactions");
    }
}
