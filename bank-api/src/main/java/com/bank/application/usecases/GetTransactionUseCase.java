package com.bank.application.usecases;

import java.util.List;

import org.springframework.stereotype.Service;

import com.bank.application.ports.AccountRepositoryPort;
import com.bank.application.ports.TransactionRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.Transaction;
import com.bank.domain.entities.TransactionStatus;

@Service
public class GetTransactionUseCase {

    private final TransactionRepositoryPort transactionRepository;
    private final AccountRepositoryPort accountRepository;
    private final AuthContextService authContextService;

    public GetTransactionUseCase(TransactionRepositoryPort transactionRepository,
                                  AccountRepositoryPort accountRepository,
                                  AuthContextService authContextService) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.authContextService = authContextService;
    }

    public Transaction findById(String transactionId) {
        Transaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        if (authContextService.hasAnyRole("ANALYST", "TELLER", "SALES")) {
            return tx;
        }

        String relatedClientId = authContextService.currentRelatedClientIdOrThrow();
        List<String> clientAccountNumbers = accountRepository.findByClientId(relatedClientId).stream()
                .map(a -> a.getAccountNumber().value())
                .toList();

        boolean involved = clientAccountNumbers.contains(tx.getSourceAccount())
                || clientAccountNumbers.contains(tx.getDestinationAccount());

        if (!involved) {
            throw new SecurityException("Not authorized to access this transaction");
        }
        return tx;
    }

    public List<Transaction> findByStatus(TransactionStatus status) {
        List<Transaction> all = transactionRepository.findAll();
        List<Transaction> filtered = all.stream()
                .filter(tx -> tx.getStatus() == status)
                .toList();

        if (authContextService.hasAnyRole("ANALYST", "TELLER", "SALES")) {
            return filtered;
        }

        String relatedClientId = authContextService.currentRelatedClientIdOrThrow();
        List<String> clientAccountNumbers = accountRepository.findByClientId(relatedClientId).stream()
                .map(a -> a.getAccountNumber().value())
                .toList();

        return filtered.stream()
                .filter(tx -> clientAccountNumbers.contains(tx.getSourceAccount())
                        || clientAccountNumbers.contains(tx.getDestinationAccount()))
                .toList();
    }
}
