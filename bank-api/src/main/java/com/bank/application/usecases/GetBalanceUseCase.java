package com.bank.application.usecases;

import org.springframework.stereotype.Service;

import com.bank.application.ports.ClientRepositoryPort;
import com.bank.application.ports.AccountRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.Account;
import com.bank.domain.services.AccountService;
import com.bank.domain.valueobjects.Money;

@Service
public class GetBalanceUseCase {

    private final AccountRepositoryPort accountRepository;
    private final ClientRepositoryPort clientRepository;
    private final AccountService serviceAccount;
    private final AuthContextService authContextService;

    public GetBalanceUseCase(AccountRepositoryPort accountRepository,
                                 ClientRepositoryPort clientRepository,
                                 AccountService serviceAccount,
                                 AuthContextService authContextService) {
        this.accountRepository = accountRepository;
        this.clientRepository = clientRepository;
        this.serviceAccount = serviceAccount;
        this.authContextService = authContextService;
    }

    public Money execute(String accountId, String identificationIdClient) {
        return accountRepository.findById(accountId)
                .map(account -> {
                    validateAccesoAccount(account, identificationIdClient);
                    return account;
                })
                .map(serviceAccount::getBalance)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    }

    public Account getAccount(String accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    }

    private void validateAccesoAccount(Account account, String identificationIdClient) {
        String clientIdAccount = account.getClientId();

        if (authContextService.hasRole("TELLER")) {
            if (identificationIdClient == null || identificationIdClient.isBlank()) {
                throw new IllegalArgumentException("Client identification is required for teller operations");
            }
            var client = clientRepository.findById(clientIdAccount)
                    .orElseThrow(() -> new IllegalArgumentException("Client associated with the account was not found"));
            if (!client.getIdIdentification().equals(identificationIdClient)) {
                throw new SecurityException("Client identification does not match the account");
            }
            return;
        }

        if (authContextService.hasAnyRole("NATURAL_CLIENT", "BUSINESS_CLIENT", "COMPANY_EMPLOYEE", "COMPANY_SUPERVISOR", "SALES")) {
            String clientRelated = authContextService.currentRelatedClientIdOrThrow();
            if (!clientRelated.equals(clientIdAccount)) {
                throw new SecurityException("Not authorized to access this account");
            }
        }
    }
}
