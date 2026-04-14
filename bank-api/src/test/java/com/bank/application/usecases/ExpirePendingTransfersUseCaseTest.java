package com.bank.application.usecases;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.bank.application.ports.AuditLogEntry;
import com.bank.application.ports.AuditLogRepositoryPort;
import com.bank.application.ports.TransactionRepositoryPort;
import com.bank.domain.entities.TransactionStatus;
import com.bank.domain.entities.TransactionType;
import com.bank.domain.entities.Transaction;
import com.bank.domain.valueobjects.Money;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExpirePendingTransfersUseCaseTest {

    @Test
    void debeVencerTransferPendingFueraDeVentanaYRegisterAuditLog() {
        FakeTransactionRepository transactionRepo = new FakeTransactionRepository();
        transactionRepo.storage.add(new Transaction(
                "tx-v1",
                TransactionType.TRANSFER,
                Money.positive(BigDecimal.valueOf(300)),
                LocalDateTime.now().minusMinutes(61),
                null,
                "10000111",
                "10000112",
                TransactionStatus.AWAITING_APPROVAL,
                123L,
                null
        ));

        FakeAuditLogRepository auditLogRepo = new FakeAuditLogRepository();
        ExpirePendingTransfersUseCase useCase = new ExpirePendingTransfersUseCase(
                transactionRepo,
                auditLogRepo,
                60
        );

        useCase.execute();

        Transaction updated = transactionRepo.findById("tx-v1").orElseThrow();
        assertEquals(TransactionStatus.EXPIRED, updated.getStatus());
        assertEquals(1, auditLogRepo.storage.size());
        assertEquals("Expired_Transfer", auditLogRepo.storage.getFirst().typeOperacion());
        assertEquals("expired due to missing approval within the allowed time",
                auditLogRepo.storage.getFirst().datosDetalle().get("reason"));
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
