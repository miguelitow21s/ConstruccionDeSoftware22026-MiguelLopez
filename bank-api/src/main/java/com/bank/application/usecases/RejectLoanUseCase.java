package com.bank.application.usecases;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.application.ports.AuditLogEntry;
import com.bank.application.ports.AuditLogRepositoryPort;
import com.bank.application.ports.LoanRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.Loan;

@Service
public class RejectLoanUseCase {

    private final LoanRepositoryPort loanRepository;
    private final AuditLogRepositoryPort auditLogRepository;
    private final AuthContextService authContextService;

    public RejectLoanUseCase(LoanRepositoryPort loanRepository,
                                   AuditLogRepositoryPort auditLogRepository,
                                   AuthContextService authContextService) {
        this.loanRepository = loanRepository;
        this.auditLogRepository = auditLogRepository;
        this.authContextService = authContextService;
    }

    @Transactional
    public Loan execute(String loanId) {
                if (!authContextService.hasRole("ANALYST")) {
                        throw new SecurityException("Not authorized to reject loans");
                }

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found"));

        loan.reject();
        Loan saved = loanRepository.save(loan);

        auditLogRepository.save(new AuditLogEntry(
                UUID.randomUUID().toString(),
                "Rejection_Loan",
                LocalDateTime.now(),
                authContextService.currentUserId(),
                authContextService.currentRole(),
                saved.getId(),
                Map.of(
                        "userIdRejection", authContextService.currentUserId(),
                        "rejectionDate", LocalDateTime.now().toString(),
                        "statusAnterior", "UNDER_REVIEW",
                        "newStatus", saved.getStatus().name()
                )
        ));

        return saved;
    }
}
