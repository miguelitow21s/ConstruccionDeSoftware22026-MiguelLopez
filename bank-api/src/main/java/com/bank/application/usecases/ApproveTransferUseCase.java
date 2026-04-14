package com.bank.application.usecases;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.application.ports.AuditLogEntry;
import com.bank.application.ports.AuditLogRepositoryPort;
import com.bank.application.ports.AccountRepositoryPort;
import com.bank.application.ports.TransactionRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.TransactionStatus;
import com.bank.domain.services.TransferService;

@Service
public class ApproveTransferUseCase {

    private final TransactionRepositoryPort transactionRepository;
    private final AccountRepositoryPort accountRepository;
    private final TransferService transferService;
    private final AuditLogRepositoryPort auditLogRepository;
    private final AuthContextService authContextService;

    public ApproveTransferUseCase(TransactionRepositoryPort transactionRepository,
                                       AccountRepositoryPort accountRepository,
                                       TransferService transferService,
                                       AuditLogRepositoryPort auditLogRepository,
                                       AuthContextService authContextService) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.transferService = transferService;
        this.auditLogRepository = auditLogRepository;
        this.authContextService = authContextService;
    }

    @Transactional
    public void approve(String transactionId) {
        validateApproverRole();

        var transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        if (transaction.getStatus() != TransactionStatus.AWAITING_APPROVAL) {
            throw new IllegalStateException("The transaction is not awaiting approval");
        }

        var source = accountRepository.findByAccountNumber(transaction.getSourceAccount())
                .orElseThrow(() -> new IllegalArgumentException("Source account not found"));
        var destination = accountRepository.findByAccountNumber(transaction.getDestinationAccount())
                .orElseThrow(() -> new IllegalArgumentException("Destination account not found"));

        validateSupervisorCompany(source.getClientId());

        transferService.executePendingTransfer(transaction, source, destination, currentNumericUser());
        accountRepository.save(source);
        accountRepository.save(destination);
        transactionRepository.save(transaction);

        auditLogRepository.save(new AuditLogEntry(
            UUID.randomUUID().toString(),
            "Transfer_Approved",
            LocalDateTime.now(),
            currentUser(),
            currentRole(),
            transaction.getId(),
            Map.of(
                "creatorUserId", transaction.getCreatorUserId(),
                "creationDate", transaction.getDate().toString(),
                "approverUserId", transaction.getApproverUserId(),
                "approvalDate", transaction.getApprovalDate() != null ? transaction.getApprovalDate().toString() : "",
                "finalStatus", transaction.getStatus().name(),
                "sourceAccount", transaction.getSourceAccount(),
                "destinationAccount", transaction.getDestinationAccount(),
                "amount", transaction.getAmount().value()
            )
        ));
    }

    @Transactional
    public void reject(String transactionId) {
        validateApproverRole();

        var transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        if (transaction.getStatus() != TransactionStatus.AWAITING_APPROVAL) {
            throw new IllegalStateException("The transaction is not awaiting approval");
        }

        var source = accountRepository.findByAccountNumber(transaction.getSourceAccount())
            .orElseThrow(() -> new IllegalArgumentException("Source account not found"));
        validateSupervisorCompany(source.getClientId());

        Long approverUserId = currentNumericUser();
        transaction.reject(approverUserId);
        transactionRepository.save(transaction);

        auditLogRepository.save(new AuditLogEntry(
                UUID.randomUUID().toString(),
            "Transfer_Rejected",
                LocalDateTime.now(),
                currentUser(),
                currentRole(),
                transaction.getId(),
                Map.of(
                    "creatorUserId", transaction.getCreatorUserId(),
                    "creationDate", transaction.getDate().toString(),
                    "approverUserId", approverUserId,
                    "rejectionDate", LocalDateTime.now().toString(),
                        "finalStatus", transaction.getStatus().name(),
                "reason", "Rejected by approver"
                )
        ));
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

    private void validateApproverRole() {
        if (!authContextService.hasRole("COMPANY_SUPERVISOR")) {
            throw new SecurityException("Only Company Supervisor can approve or reject transfers");
        }
    }

    private void validateSupervisorCompany(String clientCompanySource) {
        String companySupervisor = authContextService.currentRelatedClientIdOrThrow();
        if (!companySupervisor.equals(clientCompanySource)) {
            throw new SecurityException("Not authorized to approve or reject operations from another company");
        }
    }
}
