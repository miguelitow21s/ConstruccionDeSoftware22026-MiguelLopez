package com.bank.application.usecases;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.application.ports.AccountRepositoryPort;
import com.bank.application.ports.AuditLogEntry;
import com.bank.application.ports.AuditLogRepositoryPort;
import com.bank.application.ports.TransactionRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.Transaction;
import com.bank.domain.services.TransferService;
import com.bank.domain.valueobjects.Money;

@Service
public class TransferMoneyUseCase {

    private final AccountRepositoryPort accountRepository;
    private final TransactionRepositoryPort transactionRepository;
    private final TransferService transferService;
    private final AuditLogRepositoryPort auditLogRepository;
    private final AuthContextService authContextService;
    private final BigDecimal approvalThreshold;

    public TransferMoneyUseCase(AccountRepositoryPort accountRepository,
                                   TransactionRepositoryPort transactionRepository,
                                   TransferService transferService,
                                   AuditLogRepositoryPort auditLogRepository,
                                   AuthContextService authContextService,
                                   @Value("${bank.transfer.approval-threshold}") BigDecimal approvalThreshold) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.transferService = transferService;
        this.auditLogRepository = auditLogRepository;
        this.authContextService = authContextService;
        this.approvalThreshold = approvalThreshold;
    }

    @Transactional
    public Transaction execute(String sourceAccountId, String destinationAccountId, BigDecimal amount, boolean isBusinessOperation) {
        if (!authContextService.hasAnyRole("NATURAL_CLIENT", "BUSINESS_CLIENT", "COMPANY_EMPLOYEE")) {
            throw new SecurityException("Not authorized to create transfers");
        }

        var source = accountRepository.findById(sourceAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Source account not found"));
        var destination = accountRepository.findById(destinationAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Destination account not found"));

        String clientRelated = authContextService.currentRelatedClientIdOrThrow();
        if (!clientRelated.equals(source.getClientId())) {
            throw new SecurityException("Not authorized to operate the source account");
        }

        Money money = Money.positive(amount);
        // Company employees always operate under business rules and approval threshold checks.
        boolean effectiveBusinessOperation = isBusinessOperation || authContextService.hasRole("COMPANY_EMPLOYEE");
        boolean requiresApproval = effectiveBusinessOperation && amount.compareTo(approvalThreshold) > 0;
        Long creatorUserId = currentNumericUser();

        Transaction transaction = transferService.transfer(source, destination, money, requiresApproval, creatorUserId);
        accountRepository.save(source);
        accountRepository.save(destination);
        Transaction saved = transactionRepository.save(transaction);

        auditLogRepository.save(new AuditLogEntry(
                UUID.randomUUID().toString(),
            "Transfer_Created",
                LocalDateTime.now(),
                currentUser(),
                currentRole(),
                saved.getId(),
                Map.of(
                    "creatorUserId", creatorUserId,
                    "creationDate", saved.getDate().toString(),
                        "sourceAccount", saved.getSourceAccount(),
                        "destinationAccount", saved.getDestinationAccount(),
                        "amount", saved.getAmount().value(),
                        "status", saved.getStatus().name(),
                        "requiresApproval", requiresApproval
                )
        ));
        return saved;
    }

    private String currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }

    private String currentRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities().isEmpty()) {
            return "SYSTEM";
        }
        return auth.getAuthorities().iterator().next().getAuthority();
    }

    private Long currentNumericUser() {
        return (long) Math.abs(currentUser().hashCode());
    }
}
