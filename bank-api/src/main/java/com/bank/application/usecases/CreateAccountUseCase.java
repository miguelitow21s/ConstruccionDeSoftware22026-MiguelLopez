package com.bank.application.usecases;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.bank.application.ports.ClientRepositoryPort;
import com.bank.application.ports.AccountRepositoryPort;
import com.bank.application.ports.BankingProductRepositoryPort;
import com.bank.application.ports.SystemUserRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.ProductCategory;
import com.bank.domain.entities.UserStatus;
import com.bank.domain.entities.Account;
import com.bank.domain.entities.AccountType;
import com.bank.domain.valueobjects.Money;
import com.bank.domain.valueobjects.AccountNumber;

@Service
public class CreateAccountUseCase {

    private final AccountRepositoryPort accountRepository;
    private final ClientRepositoryPort clientRepository;
    private final SystemUserRepositoryPort systemUserRepository;
    private final BankingProductRepositoryPort bankingProductRepository;
    private final AuthContextService authContextService;

    public CreateAccountUseCase(AccountRepositoryPort accountRepository,
                              ClientRepositoryPort clientRepository,
                              SystemUserRepositoryPort systemUserRepository,
                              BankingProductRepositoryPort bankingProductRepository,
                              AuthContextService authContextService) {
        this.accountRepository = accountRepository;
        this.clientRepository = clientRepository;
        this.systemUserRepository = systemUserRepository;
        this.bankingProductRepository = bankingProductRepository;
        this.authContextService = authContextService;
    }

    public Account execute(String accountNumber, BigDecimal initialBalance, AccountType accountType, String clientId) {
        if (!authContextService.hasAnyRole("ANALYST", "TELLER", "SALES")) {
            throw new SecurityException("Not authorized to open accounts");
        }

        if (authContextService.hasRole("SALES")) {
            String clientRelated = authContextService.currentRelatedClientIdOrThrow();
            if (!clientRelated.equals(clientId)) {
                throw new SecurityException("Not authorized to open accounts for clients outside their scope");
            }
        }

        if (initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }
        if (accountType == null) {
            throw new IllegalArgumentException("Account type is required");
        }

        var client = clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        validateActiveClient(client.getIdIdentification());
        validateAccountTypeEnCatalogo(accountType);

        accountRepository.findByAccountNumber(accountNumber).ifPresent(existing -> {
            throw new IllegalArgumentException("Account number already exists");
        });

        Account account = new Account(
                new AccountNumber(accountNumber),
                new Money(initialBalance),
                accountType,
            clientId,
            client.getIdIdentification(),
            "COP",
            LocalDate.now()
        );
        return accountRepository.save(account);
    }

    private void validateActiveClient(String identificationIdClient) {
        var systemUser = systemUserRepository.findByIdIdentification(identificationIdClient)
                .orElseThrow(() -> new IllegalStateException("No system user is associated with the client"));

        if (systemUser.getUserStatus() != UserStatus.ACTIVE) {
            throw new IllegalStateException("Cannot open an account for an inactive or blocked client");
        }
    }

    private void validateAccountTypeEnCatalogo(AccountType accountType) {
        var productoAccount = bankingProductRepository.findByProductCode(accountType.name())
                .orElseThrow(() -> new IllegalArgumentException("Account type does not exist in the banking catalog"));

        if (productoAccount.getCategoria() != ProductCategory.ACCOUNTS) {
            throw new IllegalArgumentException("Account type does not match the accounts category");
        }
    }
}
