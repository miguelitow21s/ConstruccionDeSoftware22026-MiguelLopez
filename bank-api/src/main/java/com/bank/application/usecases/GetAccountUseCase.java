package com.bank.application.usecases;

import org.springframework.stereotype.Service;

import com.bank.application.ports.AccountRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.Account;

@Service
public class GetAccountUseCase {

    private final AccountRepositoryPort accountRepository;
    private final AuthContextService authContextService;

    public GetAccountUseCase(AccountRepositoryPort accountRepository, AuthContextService authContextService) {
        this.accountRepository = accountRepository;
        this.authContextService = authContextService;
    }

    public Account execute(String accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (authContextService.hasAnyRole("ANALYST", "TELLER", "SALES")) {
            return account;
        }

        String relatedClientId = authContextService.currentRelatedClientIdOrThrow();
        if (!relatedClientId.equals(account.getClientId())) {
            throw new SecurityException("Not authorized to access this account");
        }
        return account;
    }
}
