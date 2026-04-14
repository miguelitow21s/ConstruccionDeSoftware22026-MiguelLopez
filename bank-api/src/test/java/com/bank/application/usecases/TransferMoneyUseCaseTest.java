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

import com.bank.application.ports.AuditLogEntry;
import com.bank.application.ports.AuditLogRepositoryPort;
import com.bank.application.ports.AccountRepositoryPort;
import com.bank.application.ports.TransactionRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.Account;
import com.bank.domain.entities.TransactionStatus;
import com.bank.domain.entities.AccountType;
import com.bank.domain.entities.Transaction;
import com.bank.domain.services.TransferService;
import com.bank.domain.valueobjects.Money;
import com.bank.domain.valueobjects.AccountNumber;

class TransferMoneyUseCaseTest {

    @Test
    void debeFallarSiRoleNoAutorizadoParaCreateTransfer() {
        FakeAccountRepository accountRepo = new FakeAccountRepository();
        accountRepo.storage.add(account("c1", "10000001", "client-1", BigDecimal.valueOf(1000)));
        accountRepo.storage.add(account("c2", "10000002", "client-2", BigDecimal.valueOf(1000)));

        TransferMoneyUseCase useCase = new TransferMoneyUseCase(
                accountRepo,
                new FakeTransactionRepository(),
                new TransferService(),
                new FakeAuditLogRepository(),
            new AuthContextService("client_natural:client-1"),
                BigDecimal.valueOf(10_000)
        );

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("analyst", "123456", "ROLE_ANALYST")
        );

        SecurityException thrown = assertThrows(
            SecurityException.class,
            () -> useCase.execute("c1", "c2", BigDecimal.valueOf(100), false)
        );
        assertEquals("Not authorized to create transfers", thrown.getMessage());
    }

    @Test
    void debePermitirTransferConRoleClientNatural() {
        FakeAccountRepository accountRepo = new FakeAccountRepository();
        accountRepo.storage.add(account("c1", "10000003", "client-1", BigDecimal.valueOf(1000)));
        accountRepo.storage.add(account("c2", "10000004", "client-2", BigDecimal.valueOf(1000)));

        FakeTransactionRepository transactionRepo = new FakeTransactionRepository();
        TransferMoneyUseCase useCase = new TransferMoneyUseCase(
                accountRepo,
                transactionRepo,
                new TransferService(),
                new FakeAuditLogRepository(),
            new AuthContextService("client_natural:client-1"),
                BigDecimal.valueOf(10_000)
        );

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("client_natural", "123456", "ROLE_NATURAL_CLIENT")
        );

        Transaction transaction = useCase.execute("c1", "c2", BigDecimal.valueOf(100), false);

        assertEquals(TransactionStatus.EXECUTED, transaction.getStatus());
        assertEquals(1, transactionRepo.storage.size());
        assertEquals(BigDecimal.valueOf(900).setScale(2), accountRepo.findById("c1").orElseThrow().getBalance().value());
        assertEquals(BigDecimal.valueOf(1100).setScale(2), accountRepo.findById("c2").orElseThrow().getBalance().value());
    }

        @Test
        void debeFallarSiAccountSourceNoPerteneceAlClientAutenticado() {
        FakeAccountRepository accountRepo = new FakeAccountRepository();
        accountRepo.storage.add(account("c1", "10000005", "client-ajeno", BigDecimal.valueOf(1000)));
        accountRepo.storage.add(account("c2", "10000006", "client-2", BigDecimal.valueOf(1000)));

        TransferMoneyUseCase useCase = new TransferMoneyUseCase(
            accountRepo,
            new FakeTransactionRepository(),
            new TransferService(),
            new FakeAuditLogRepository(),
            new AuthContextService("client_natural:client-1"),
            BigDecimal.valueOf(10_000)
        );

        SecurityContextHolder.getContext().setAuthentication(
            new TestingAuthenticationToken("client_natural", "123456", "ROLE_NATURAL_CLIENT")
        );

        SecurityException thrown = assertThrows(
            SecurityException.class,
            () -> useCase.execute("c1", "c2", BigDecimal.valueOf(100), false)
        );
        assertEquals("Not authorized to operate the source account", thrown.getMessage());
        }

    @Test
    void empleadoCompanyDebeDejarPendingTransferSobreUmbral() {
        FakeAccountRepository accountRepo = new FakeAccountRepository();
        accountRepo.storage.add(account("c1", "10000007", "company-1", BigDecimal.valueOf(10000)));
        accountRepo.storage.add(account("c2", "10000008", "proveedor-1", BigDecimal.valueOf(500)));

        FakeTransactionRepository transactionRepo = new FakeTransactionRepository();
        TransferMoneyUseCase useCase = new TransferMoneyUseCase(
                accountRepo,
                transactionRepo,
                new TransferService(),
                new FakeAuditLogRepository(),
                new AuthContextService("empleado_company:company-1"),
                BigDecimal.valueOf(2_000)
        );

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("empleado_company", "123456", "ROLE_COMPANY_EMPLOYEE")
        );

        Transaction transaction = useCase.execute("c1", "c2", BigDecimal.valueOf(5_000), true);

        assertEquals(TransactionStatus.AWAITING_APPROVAL, transaction.getStatus());
        assertEquals(1, transactionRepo.storage.size());
    }

    private Account account(String id, String number, String clientId, BigDecimal balance) {
        return new Account(
                id,
                new AccountNumber(number),
                new Money(balance),
                AccountType.SAVINGS,
                clientId,
                com.bank.domain.entities.AccountStatus.ACTIVE
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

    private static final class FakeAuditLogRepository implements AuditLogRepositoryPort {
        private final List<AuditLogEntry> storage = new ArrayList<>();

        @Override
        public void save(AuditLogEntry entry) {
            storage.add(entry);
        }

        @Override
        public List<AuditLogEntry> findAll() {
            return List.copyOf(storage);
        }

        @Override
        public List<AuditLogEntry> findByUserId(String userId) {
            return storage.stream().filter(e -> e.userId().equals(userId)).toList();
        }

        @Override
        public List<AuditLogEntry> findByIdProductoAfectadoIn(List<String> idsProductoAfectado) {
            return storage.stream().filter(e -> idsProductoAfectado.contains(e.idProductoAfectado())).toList();
        }
    }
}

