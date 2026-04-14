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
import com.bank.application.ports.AuditLogEntry;
import com.bank.application.ports.AuditLogRepositoryPort;
import com.bank.application.ports.TransactionRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.Account;
import com.bank.domain.entities.AccountStatus;
import com.bank.domain.entities.AccountType;
import com.bank.domain.entities.Transaction;
import com.bank.domain.entities.TransactionStatus;
import com.bank.domain.services.TransferService;
import com.bank.domain.valueobjects.AccountNumber;
import com.bank.domain.valueobjects.Money;

class CreateBulkPaymentsUseCaseTest {

    @Test
    void empleadoCompanyPuedeCreateBulkPaymentsYAplicarUmbral() {
        FakeAccountRepository accountRepo = new FakeAccountRepository();
        accountRepo.storage.add(account("source", "20000001", "company-1", BigDecimal.valueOf(10000)));
        accountRepo.storage.add(account("destination-1", "20000002", "empleado-1", BigDecimal.valueOf(0)));
        accountRepo.storage.add(account("destination-2", "20000003", "empleado-2", BigDecimal.valueOf(0)));

        FakeTransactionRepository transactionRepo = new FakeTransactionRepository();
        TransferMoneyUseCase transferMoneyUseCase = new TransferMoneyUseCase(
                accountRepo,
                transactionRepo,
                new TransferService(),
                new FakeAuditLogRepository(),
                new AuthContextService("empleado_company:company-1"),
                BigDecimal.valueOf(2000)
        );

        CreateBulkPaymentsUseCase useCase = new CreateBulkPaymentsUseCase(
                transferMoneyUseCase,
                new AuthContextService("empleado_company:company-1")
        );

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("empleado_company", "123456", "ROLE_COMPANY_EMPLOYEE")
        );

        List<Transaction> transactions = useCase.execute(
                "source",
                List.of(
                        new CreateBulkPaymentsUseCase.BulkPaymentItem("destination-1", BigDecimal.valueOf(2500)),
                        new CreateBulkPaymentsUseCase.BulkPaymentItem("destination-2", BigDecimal.valueOf(1500))
                )
        );

        assertEquals(2, transactions.size());
        assertEquals(TransactionStatus.AWAITING_APPROVAL, transactions.getFirst().getStatus());
        assertEquals(TransactionStatus.EXECUTED, transactions.get(1).getStatus());
    }

    @Test
    void debeFallarSiRoleNoEsEmpleadoCompany() {
        TransferMoneyUseCase transferMoneyUseCase = new TransferMoneyUseCase(
                new FakeAccountRepository(),
                new FakeTransactionRepository(),
                new TransferService(),
                new FakeAuditLogRepository(),
                new AuthContextService("supervisor_company:company-1"),
                BigDecimal.valueOf(2000)
        );

        CreateBulkPaymentsUseCase useCase = new CreateBulkPaymentsUseCase(
                transferMoneyUseCase,
                new AuthContextService("supervisor_company:company-1")
        );

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("supervisor_company", "123456", "ROLE_COMPANY_SUPERVISOR")
        );

        SecurityException thrown = assertThrows(
                SecurityException.class,
                () -> useCase.execute("source", List.of(new CreateBulkPaymentsUseCase.BulkPaymentItem("destination", BigDecimal.ONE)))
        );
        assertEquals("Not authorized to create payments bulk", thrown.getMessage());
    }

    private Account account(String id, String number, String clientId, BigDecimal balance) {
        return new Account(id, new AccountNumber(number), new Money(balance), AccountType.SAVINGS, clientId, AccountStatus.ACTIVE);
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
        @Override
        public void save(AuditLogEntry entry) {
        }

        @Override
        public List<AuditLogEntry> findAll() {
            return List.of();
        }

        @Override
        public List<AuditLogEntry> findByUserId(String userId) {
            return List.of();
        }

        @Override
        public List<AuditLogEntry> findByAffectedProductIdIn(List<String> idsProductoAfectado) {
            return List.of(); // Placeholder implementation
        }
    }
}


