package com.bank.application.usecases;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.bank.application.ports.AccountRepositoryPort;
import com.bank.application.ports.TransactionRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.Account;
import com.bank.domain.entities.AccountStatus;
import com.bank.domain.entities.Transaction;
import com.bank.domain.entities.TransactionType;
import com.bank.domain.entities.TransactionStatusntNumber;
import com.bank.domain.entities.TransactionType;
import com.bank.domain.valueobjects.Money;
import com.bank.domain.valueobjects.Money;

class ListTransactionsUseCaseTest {

    @Test
    void supervisorSoloVeTransactionsDeSuCompany() {
        FakeAccountRepository accountRepo = new FakeAccountRepository();
        accountRepo.storage.add(account("c1", "10000125", "company-1"));

        FakeTransactionRepository transactionRepo = new FakeTransactionRepository();
        transactionRepo.storage.add(transaction("t1", "10000125", "10000999"));
        transactionRepo.storage.add(transaction("t2", "10000888", "10000777"));

        ListTransactionsUseCase useCase = new ListTransactionsUseCase(
                transactionRepo,
                accountRepo,
                new AuthContextService("supervisor:company-1")
        );

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("supervisor", "123456", "ROLE_COMPANY_SUPERVISOR")
        );

        List<Transaction> resultado = useCase.execute();

        assertEquals(1, resultado.size());
        assertEquals("t1", resultado.getFirst().getId());
    }

    @Test
    void debeFiltrarPorAccountNumberDelClientAutenticado() {
        FakeAccountRepository accountRepo = new FakeAccountRepository();
        accountRepo.storage.add(account("c1", "10000123", "client-1"));

        FakeTransactionRepository transactionRepo = new FakeTransactionRepository();
        transactionRepo.storage.add(transaction("t1", "10000123", "10000999"));
        transactionRepo.storage.add(transaction("t2", "10000888", "10000777"));

        ListTransactionsUseCase useCase = new ListTransactionsUseCase(
                transactionRepo,
                accountRepo,
                new AuthContextService("client_natural:client-1")
        );

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("client_natural", "123456", "ROLE_NATURAL_CLIENT")
        );

        List<Transaction> resultado = useCase.execute();

        assertEquals(1, resultado.size());
        assertEquals("t1", resultado.getFirst().getId());
    }

        @Test
        void salesSoloVeTransactionsDeClientsBajoManagement() {
        FakeAccountRepository accountRepo = new FakeAccountRepository();
        accountRepo.storage.add(account("c1", "10000123", "client-1"));

        FakeTransactionRepository transactionRepo = new FakeTransactionRepository();
        transactionRepo.storage.add(transaction("t1", "10000123", "10000999"));
        transactionRepo.storage.add(transaction("t2", "10000888", "10000777"));

        ListTransactionsUseCase useCase = new ListTransactionsUseCase(
            transactionRepo,
            accountRepo,
            new AuthContextService("sales:client-1")
        );

        SecurityContextHolder.getContext().setAuthentication(
            new TestingAuthenticationToken("sales", "123456", "ROLE_SALES")
        );

        List<Transaction> resultado = useCase.execute();

        assertEquals(1, resultado.size());
        assertEquals("t1", resultado.getFirst().getId());
        }

        @Test
        void debeFallarParaRoleNoAutorizado() {
        ListTransactionsUseCase useCase = new ListTransactionsUseCase(
            new FakeTransactionRepository(),
            new FakeAccountRepository(),
            new AuthContextService("")
        );

        SecurityContextHolder.getContext().setAuthentication(
            new TestingAuthenticationToken("invitado", "123456", "ROLE_INVITADO")
        );

        SecurityException thrown = assertThrows(SecurityException.class, useCase::execute);
        assertEquals("Not authorized to get transactions", thrown.getMessage());
        }

    private Account account(String id, String accountNumber, String clientId) {
        return new Account(id, new AccountNumber(accountNumber), new Money(BigDecimal.valueOf(1000)), AccountType.SAVINGS, clientId, AccountStatus.ACTIVE);
    }

    private Transaction transaction(String id, String sourceAccount, String destinationAccount) {
        return new Transaction(
                id,
                TransactionType.TRANSFER,
                Money.positive(BigDecimal.valueOf(100)),
                LocalDateTime.now(),
                sourceAccount,
                destinationAccount,
                TransactionStatus.EXECUTED
        );
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
        public List<Account> findByClientId(String clientId) {
            return storage.stream().filter(c -> c.getClientId().equals(clientId)).toList();
        }
    }

    private static final class FakeTransactionRepository implements TransactionRepositoryPort {
        private final List<Transaction> storage = new ArrayList<>();

        @Override
        public Transaction save(Transaction transaction) {
            storage.removeIf(existing -> existing.getId().equals(transaction.getId()));
            storage.add(transaction);
            return transaction;
        }

        @Override
        public Optional<Transaction> findById(String id) {
            return storage.stream().filter(t -> t.getId().equals(id)).findFirst();
        }

        @Override
        public List<Transaction> findAll() {
            return List.copyOf(storage);
        }

        @Override
        public List<Transaction> findByAccountSourceInOrAccountDestinationIn(List<String> accountsSource, List<String> accountsDestination) {
            return storage.stream()
                    .filter(t -> accountsSource.contains(t.getSourceAccount()) || accountsDestination.contains(t.getDestinationAccount()))
                    .toList();
        }

        @Override
        public List<Transaction> findByStatusAndDateBefore(TransactionStatus status, LocalDateTime date) {
            return storage.stream().filter(t -> t.getStatus() == status && t.getDate().isBefore(date)).toList();
        }
    }
}

