package com.bank.application.usecases;

import com.bank.application.ports.AuditLogEntry;
import com.bank.application.ports.AuditLogRepositoryPort;
import com.bank.application.ports.TransactionRepositoryPort;
import com.bank.domain.entities.TransactionStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ExpirePendingTransfersUseCase {

    private final TransactionRepositoryPort transactionRepository;
    private final AuditLogRepositoryPort auditLogRepository;
    private final long expirationMinutes;

    public ExpirePendingTransfersUseCase(TransactionRepositoryPort transactionRepository,
                                                 AuditLogRepositoryPort auditLogRepository,
                                                 @Value("${bank.transfer.approval-expiration-minutes}") long expirationMinutes) {
        this.transactionRepository = transactionRepository;
        this.auditLogRepository = auditLogRepository;
        this.expirationMinutes = expirationMinutes;
    }

    // Checks every minute for pending transfers that exceeded the approval window.
    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void execute() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(expirationMinutes);
        List<com.bank.domain.entities.Transaction> expiring = transactionRepository
                .findByStatusAndDateBefore(TransactionStatus.AWAITING_APPROVAL, cutoff);

        expiring.forEach(transaction -> {
            transaction.expire();
            transactionRepository.save(transaction);
            auditLogRepository.save(new AuditLogEntry(
                    UUID.randomUUID().toString(),
                    "Expired_Transfer",
                    LocalDateTime.now(),
                    "system",
                    "SYSTEM",
                    transaction.getId(),
                    Map.of(
                            "creatorUserId", transaction.getCreatorUserId(),
                            "creationDate", transaction.getDate().toString(),
                            "reason", "Expired due to missing approval within the allowed time",
                            "finalStatus", transaction.getStatus().name()
                    )
            ));
        });
    }
}
