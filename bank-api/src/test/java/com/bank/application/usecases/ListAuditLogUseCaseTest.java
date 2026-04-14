package com.bank.application.usecases;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.bank.application.ports.AuditLogEntry;
import com.bank.application.ports.AuditLogRepositoryPort;
import com.bank.application.ports.AccountRepositoryPort;
import com.bank.application.ports.LoanRepositoryPort;
import com.bank.application.ports.TransactionRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.Account;
import com.bank.domain.entities.TransactionStatus;
import com.bank.domain.entities.Loan;
import com.bank.domain.entities.AccountType;
import com.bank.domain.entities.TransactionType;
import com.bank.domain.entities.Transaction;
import com.bank.domain.valueobjects.Money;
import com.bank.domain.valueobjects.AccountNumber;

class ListAuditLogUseCaseTest {

    @Test
    void analystDebePoderFiltrarPorUser() {
        FakeAuditLogRepositoryPort repo = new FakeAuditLogRepositoryPort();
        repo.storage.add(entry("u1", "b1"));
        repo.storage.add(entry("u2", "b2"));

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("analyst", "123456", "ROLE_ANALYST")
        );

    ListAuditLogUseCase useCase = new ListAuditLogUseCase(
        repo,
        new FakeAccountRepositoryPort(),
        new FakeLoanRepositoryPort(),
        new FakeTransactionRepositoryPort(),
        new AuthContextService("")
    );

        List<AuditLogEntry> resultado = useCase.execute("u1");

        assertEquals(1, resultado.size());
        assertEquals("u1", resultado.getFirst().userId());
    }

    @Test
    void analystDebePoderGetAuditLogCompleta() {
        FakeAuditLogRepositoryPort repo = new FakeAuditLogRepositoryPort();
        repo.storage.add(entry("u1", "b1"));
        repo.storage.add(entry("u2", "b2"));

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("analyst", "123456", "ROLE_ANALYST")
        );

        ListAuditLogUseCase useCase = new ListAuditLogUseCase(
            repo,
            new FakeAccountRepositoryPort(),
            new FakeLoanRepositoryPort(),
            new FakeTransactionRepositoryPort(),
            new AuthContextService("")
        );

        List<AuditLogEntry> resultado = useCase.execute(null);

        assertEquals(2, resultado.size());
    }

    @Test
        void clientDebeVerSoloAuditLogDeSusProductos() {
        FakeAuditLogRepositoryPort repo = new FakeAuditLogRepositoryPort();
        repo.storage.add(entry("c1", "b1", "tx-client"));
        repo.storage.add(entry("c2", "b2", "tx-ajena"));

        FakeAccountRepositoryPort accounts = new FakeAccountRepositoryPort();
        accounts.storage.add(new Account("account-1", new AccountNumber("11110000"), new Money(java.math.BigDecimal.valueOf(2000)), AccountType.SAVINGS, "client-1", com.bank.domain.entities.AccountStatus.ACTIVE));

        FakeTransactionRepositoryPort transactions = new FakeTransactionRepositoryPort();
        transactions.storage.add(new Transaction("tx-client", TransactionType.TRANSFER, new Money(java.math.BigDecimal.valueOf(100)), LocalDateTime.now().minusMinutes(1), "11110000", "99990000", TransactionStatus.EXECUTED));

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("client_natural", "123456", "ROLE_NATURAL_CLIENT")
        );

        ListAuditLogUseCase useCase = new ListAuditLogUseCase(
            repo,
            accounts,
            new FakeLoanRepositoryPort(),
            transactions,
            new AuthContextService("client_natural:client-1")
        );

        List<AuditLogEntry> resultado = useCase.execute(null, null);

        assertEquals(1, resultado.size());
        assertEquals("tx-client", resultado.getFirst().idProductoAfectado());
        }

        @Test
        void clientNoDebeGetAuditLogDeProductoAjeno() {
        FakeAuditLogRepositoryPort repo = new FakeAuditLogRepositoryPort();
        FakeAccountRepositoryPort accounts = new FakeAccountRepositoryPort();
        accounts.storage.add(new Account("account-1", new AccountNumber("11110000"), new Money(java.math.BigDecimal.valueOf(2000)), AccountType.SAVINGS, "client-1", com.bank.domain.entities.AccountStatus.ACTIVE));

        SecurityContextHolder.getContext().setAuthentication(
            new TestingAuthenticationToken("client_natural", "123456", "ROLE_NATURAL_CLIENT")
        );

        ListAuditLogUseCase useCase = new ListAuditLogUseCase(
            repo,
            accounts,
            new FakeLoanRepositoryPort(),
            new FakeTransactionRepositoryPort(),
            new AuthContextService("client_natural:client-1")
        );

        SecurityException thrown = assertThrows(SecurityException.class, () -> {
            var ignored = useCase.execute(null, "producto-ajeno");
            assertEquals(0, ignored.size());
        });
        assertEquals("Not authorized to access audit logs for unrelated products", thrown.getMessage());
        }

        @Test
        void tellerNoDebeGetAuditLog() {
        SecurityContextHolder.getContext().setAuthentication(
            new TestingAuthenticationToken("teller", "123456", "ROLE_TELLER")
        );

        ListAuditLogUseCase useCase = new ListAuditLogUseCase(
            new FakeAuditLogRepositoryPort(),
            new FakeAccountRepositoryPort(),
            new FakeLoanRepositoryPort(),
            new FakeTransactionRepositoryPort(),
            new AuthContextService("")
        );

        SecurityException thrown = assertThrows(SecurityException.class, () -> {
            var ignored = useCase.execute(null, null);
            assertEquals(0, ignored.size());
        });
        assertEquals("Not authorized to get audit log", thrown.getMessage());
    }

    private AuditLogEntry entry(String userId, String idAuditLog) {
        return entry(userId, idAuditLog, "producto-1");
        }

        private AuditLogEntry entry(String userId, String idAuditLog, String idProducto) {
        return new AuditLogEntry(
                idAuditLog,
                "Transfer_Creada",
                LocalDateTime.now(),
                userId,
                "ROLE_NATURAL_CLIENT",
            idProducto,
                Map.of("amount", 100)
        );
    }

    private static final class FakeAuditLogRepositoryPort implements AuditLogRepositoryPort {

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

    private static final class FakeAccountRepositoryPort implements AccountRepositoryPort {
        private final List<Account> storage = new ArrayList<>();

        @Override
        public Account save(Account account) {
            storage.removeIf(existing -> existing.getId().equals(account.getId()));
            storage.add(account);
            return account;
        }

        @Override
        public java.util.Optional<Account> findById(String id) {
            return storage.stream().filter(c -> c.getId().equals(id)).findFirst();
        }

        @Override
        public java.util.Optional<Account> findByAccountNumber(String accountNumber) {
            return storage.stream().filter(c -> c.getAccountNumber().value().equals(accountNumber)).findFirst();
        }

        @Override
        public List<Account> findByClientId(String clientId) {
            return storage.stream().filter(c -> c.getClientId().equals(clientId)).toList();
        }
    }

    private static final class FakeLoanRepositoryPort implements LoanRepositoryPort {
        private final List<Loan> storage = new ArrayList<>();

        @Override
        public Loan save(Loan loan) {
            storage.removeIf(existing -> existing.getId().equals(loan.getId()));
            storage.add(loan);
            return loan;
        }

        @Override
        public java.util.Optional<Loan> findById(String id) {
            return storage.stream().filter(p -> p.getId().equals(id)).findFirst();
        }

        @Override
        public List<Loan> findAll() {
            return List.copyOf(storage);
        }

        @Override
        public List<Loan> findByClientSolicitanteId(String applicantClientId) {
            return storage.stream().filter(p -> p.getApplicantClientId().equals(applicantClientId)).toList();
        }
    }

    private static final class FakeTransactionRepositoryPort implements TransactionRepositoryPort {
        private final List<Transaction> storage = new ArrayList<>();

        @Override
        public Transaction save(Transaction transaction) {
            storage.removeIf(existing -> existing.getId().equals(transaction.getId()));
            storage.add(transaction);
            return transaction;
        }

        @Override
        public java.util.Optional<Transaction> findById(String id) {
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

