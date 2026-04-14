package com.bank.application.usecases;

import java.util.List;

import org.springframework.stereotype.Service;

import com.bank.application.ports.AccountRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.Account;

@Service
public class ListAccountsUseCase {

    private final AccountRepositoryPort accountRepository;
    private final AuthContextService authContextService;

    public ListAccountsUseCase(AccountRepositoryPort accountRepository,
                               AuthContextService authContextService) {
        this.accountRepository = accountRepository;
        this.authContextService = authContextService;
    }

    public List<Account> execute() {
        if (authContextService.hasRole("ANALYST")) {
            return accountRepository.findAll();
        }

        if (!authContextService.hasAnyRole("NATURAL_CLIENT", "BUSINESS_CLIENT", "COMPANY_EMPLOYEE", "COMPANY_SUPERVISOR", "SALES")) {
            throw new SecurityException("Not authorized to list accounts");
        }

        String clientId = authContextService.currentRelatedClientIdOrThrow();
        return accountRepository.findByClientId(clientId);
    }
}
