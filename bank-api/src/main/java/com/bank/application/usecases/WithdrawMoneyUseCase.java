package com.bank.application.usecases;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.application.ports.ClientRepositoryPort;
import com.bank.application.ports.AccountRepositoryPort;
import com.bank.application.ports.TransactionRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.TransactionStatus;
import com.bank.domain.entities.TransactionType;
import com.bank.domain.entities.Transaction;
import com.bank.domain.services.AccountService;
import com.bank.domain.valueobjects.Money;

@Service
public class WithdrawMoneyUseCase {

    private final AccountRepositoryPort accountRepository;
    private final ClientRepositoryPort clientRepository;
    private final TransactionRepositoryPort transactionRepository;
    private final AccountService serviceAccount;
    private final AuthContextService authContextService;

    public WithdrawMoneyUseCase(AccountRepositoryPort accountRepository,
                                ClientRepositoryPort clientRepository,
                                TransactionRepositoryPort transactionRepository,
                                AccountService serviceAccount,
                                AuthContextService authContextService) {
        this.accountRepository = accountRepository;
        this.clientRepository = clientRepository;
        this.transactionRepository = transactionRepository;
        this.serviceAccount = serviceAccount;
        this.authContextService = authContextService;
    }

    @Transactional
    public void execute(String accountId, String identificationIdClient, BigDecimal amount) {
        if (!authContextService.hasRole("TELLER")) {
            throw new SecurityException("Not authorized to realizar withdrawals");
        }

        var account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (identificationIdClient == null || identificationIdClient.isBlank()) {
            throw new IllegalArgumentException("Client identification is required for teller operations");
        }

        var client = clientRepository.findById(account.getClientId())
                .orElseThrow(() -> new IllegalArgumentException("Client associated with the account was not found"));

        if (!client.getIdIdentification().equals(identificationIdClient)) {
            throw new SecurityException("Client identification does not match the account");
        }

        Money money = Money.positive(amount);
        serviceAccount.withdraw(account, money);
        accountRepository.save(account);

        Transaction transaction = new Transaction(
                TransactionType.WITHDRAWAL,
                money,
                account.getAccountNumber().value(),
                account.getAccountNumber().value(),
                TransactionStatus.EXECUTED
        );
        transactionRepository.save(transaction);
    }
}
