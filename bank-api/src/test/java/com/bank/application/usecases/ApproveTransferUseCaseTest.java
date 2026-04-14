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
import com.bank.domain.entities.TransactionType;
import com.bank.domain.services.TransferService;
import com.bank.domain.valueobjects.AccountNumber;
import com.bank.domain.valueobjects.Money;

class ApproveTransferUseCaseTest {

    @Test
    void debeFallarApprovalSiNoEsSupervisorCompany() {
        FakeTransactionRepository transactionRepo = new FakeTransactionRepository();
        transactionRepo.storage.add(transactionEnEspera("t-1"));

        ApproveTransferUseCase useCase = new ApproveTransferUseCase(
                transactionRepo,
                new FakeAccountRepository(),
                new TransferService(),
                new FakeAuditLogRepository(),
                new AuthContextService("")
        );

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("analyst", "123456", "ROLE_ANALYST")
        );

        SecurityException thrown = assertThrows(SecurityException.class, () -> useCase.approve("t-1"));
        assertEquals("Only Company Supervisor can approve or reject transfers", thrown.getMessage());
    }

    @Test
    void supervisorDebePoderRechazarTransferEnEspera() {
        FakeTransactionRepository transactionRepo = new FakeTransactionRepository();
        transactionRepo.storage.add(transactionEnEspera("t-2"));
        FakeAccountRepository accountRepo = new FakeAccountRepository();
        accountRepo.storage.add(account("c-source", "10000111", "company-1"));
        accountRepo.storage.add(account("c-destination", "10000112", "company-destination"));

        FakeAuditLogRepository auditLogRepo = new FakeAuditLogRepository();
        ApproveTransferUseCase useCase = new ApproveTransferUseCase(
                transactionRepo,
            accountRepo,
                new TransferService(),
                auditLogRepo,
            new AuthContextService("supervisor:company-1")
        );

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("supervisor", "123456", "ROLE_COMPANY_SUPERVISOR")
        );

        useCase.reject("t-2");

        Transaction updated = transactionRepo.findById("t-2").orElseThrow();
        assertEquals(TransactionStatus.REJECTED, updated.getStatus());
        assertEquals((Long) (long) Math.abs("supervisor".hashCode()), updated.getApproverUserId());
        assertEquals(1, auditLogRepo.storage.size());
        assertEquals("Transfer_Rechazada", auditLogRepo.storage.getFirst().operationType());
    }

    @Test
    void supervisorDebePoderApproveTransferYRegisterTrazabilidad() {
        FakeTransactionRepository transactionRepo = new FakeTransactionRepository();
        transactionRepo.storage.add(transactionEnEspera("t-4"));
        FakeAccountRepository accountRepo = new FakeAccountRepository();
        accountRepo.storage.add(account("c-source", "10000111", "company-1"));
        accountRepo.storage.add(account("c-destination", "10000112", "company-destination"));

        FakeAuditLogRepository auditLogRepo = new FakeAuditLogRepository();
        ApproveTransferUseCase useCase = new ApproveTransferUseCase(
                transactionRepo,
                accountRepo,
                new TransferService(),
                auditLogRepo,
                new AuthContextService("supervisor:company-1")
        );

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("supervisor", "123456", "ROLE_COMPANY_SUPERVISOR")
        );

        useCase.approve("t-4");

        Transaction updated = transactionRepo.findById("t-4").orElseThrow();
        assertEquals(TransactionStatus.EXECUTED, updated.getStatus());
        assertEquals((Long) (long) Math.abs("supervisor".hashCode()), updated.getApproverUserId());
        assertEquals(1, auditLogRepo.storage.size());
        assertEquals("Transfer_Approved", auditLogRepo.storage.getFirst().operationType());
    }

    @Test
    void supervisorNoPuedeApproveTransferDeOtraCompany() {
        FakeTransactionRepository transactionRepo = new FakeTransactionRepository();
        transactionRepo.storage.add(transactionEnEspera("t-3"));
        FakeAccountRepository accountRepo = new FakeAccountRepository();
        accountRepo.storage.add(account("c-source", "10000111", "company-ajena"));
        accountRepo.storage.add(account("c-destination", "10000112", "company-destination"));

        ApproveTransferUseCase useCase = new ApproveTransferUseCase(
                transactionRepo,
                accountRepo,
                new TransferService(),
                new FakeAuditLogRepository(),
                new AuthContextService("supervisor:company-1")
        );

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("supervisor", "123456", "ROLE_COMPANY_SUPERVISOR")
        );

        SecurityException thrown = assertThrows(SecurityException.class, () -> useCase.approve("t-3"));
        assertEquals("Not authorized to approve or reject operations from another company", thrown.getMessage());
    }

    private Transaction transactionEnEspera(String id) {
        return new Transaction(
                id,
                TransactionType.TRANSFER,
                Money.positive(BigDecimal.valueOf(500)),
                LocalDateTime.now(),
                "10000111",
                "10000112",
                TransactionStatus.AWAITING_APPROVAL
        );
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

    private Account account(String id, String number, String clientId) {
        return new Account(
                id,
                new AccountNumber(number),
                new Money(BigDecimal.valueOf(1000)),
                AccountType.SAVINGS,
                clientId,
                AccountStatus.ACTIVE
        );
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
        public List<AuditLogEntry> findByAffectedProductIdIn(List<String> idsProductoAfectado) {
            return storage.stream().filter(e -> idsProductoAfectado.contains(e.affectedProductId())).toList();
        }
    }
}


