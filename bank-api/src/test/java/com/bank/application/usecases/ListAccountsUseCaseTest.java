package com.bank.application.usecases;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.bank.application.ports.AccountRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.Account;
import com.bank.domain.entities.AccountStatus;
import com.bank.domain.entities.AccountType;
import com.bank.domain.valueobjects.AccountNumber;
import com.bank.domain.valueobjects.Money;

class ListAccountsUseCaseTest {

    @Test
    void analystPuedeListarTodasLasAccounts() {
        FakeAccountRepository accountRepo = new FakeAccountRepository();
        accountRepo.storage.add(account("a1", "10000555", "client-1"));
        accountRepo.storage.add(account("a2", "10000666", "client-2"));

        ListAccountsUseCase useCase = new ListAccountsUseCase(
                accountRepo,
                new AuthContextService("")
        );

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("analyst", "123456", "ROLE_ANALYST")
        );

        List<Account> resultado = useCase.execute();

        assertEquals(2, resultado.size());
    }

    @Test
    void supervisorSoloVeAccountsDeSuCompany() {
        FakeAccountRepository accountRepo = new FakeAccountRepository();
        accountRepo.storage.add(account("a1", "10000333", "company-1"));
        accountRepo.storage.add(account("a2", "10000444", "company-2"));

        ListAccountsUseCase useCase = new ListAccountsUseCase(
                accountRepo,
                new AuthContextService("supervisor:company-1")
        );

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("supervisor", "123456", "ROLE_COMPANY_SUPERVISOR")
        );

        List<Account> resultado = useCase.execute();

        assertEquals(1, resultado.size());
        assertEquals("company-1", resultado.getFirst().getClientId());
    }

    @Test
    void salesSoloVeAccountsDeClientsBajoManagement() {
        FakeAccountRepository accountRepo = new FakeAccountRepository();
        accountRepo.storage.add(account("a1", "10000111", "client-1"));
        accountRepo.storage.add(account("a2", "10000222", "client-2"));

        ListAccountsUseCase useCase = new ListAccountsUseCase(
                accountRepo,
                new AuthContextService("sales:client-1")
        );

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("sales", "123456", "ROLE_SALES")
        );

        List<Account> resultado = useCase.execute();

        assertEquals(1, resultado.size());
        assertEquals("client-1", resultado.getFirst().getClientId());
    }

    @Test
    void tellerNoDebeListAccounts() {
        ListAccountsUseCase useCase = new ListAccountsUseCase(
                new FakeAccountRepository(),
                new AuthContextService("")
        );

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("teller", "123456", "ROLE_TELLER")
        );

        SecurityException thrown = assertThrows(SecurityException.class, useCase::execute);
        assertEquals("Not authorized to list accounts", thrown.getMessage());
    }

    private Account account(String id, String number, String clientId) {
        return new Account(id, new AccountNumber(number), new Money(BigDecimal.valueOf(1000)), AccountType.SAVINGS, clientId, AccountStatus.ACTIVE);
    }

    private static final class FakeAccountRepository implements AccountRepositoryPort {
        private final List<Account> storage = new ArrayList<>();

        @Override
        public Account save(Account account) {
            storage.removeIf(existing -> existing.getId().equals(account.getId()));
            storage.add(account);
            return account;
        }

        @Override
        public Optional<Account> findById(String id) {
            return storage.stream().filter(c -> c.getId().equals(id)).findFirst();
        }

        @Override
        public Optional<Account> findByAccountNumber(String accountNumber) {
            return storage.stream().filter(c -> c.getAccountNumber().value().equals(accountNumber)).findFirst();
        }

        @Override
        public List<Account> findAll() {
            return List.copyOf(storage);
        }

        @Override
        public List<Account> findByClientId(String clientId) {
            return storage.stream().filter(c -> c.getClientId().equals(clientId)).toList();
        }
    }
}
