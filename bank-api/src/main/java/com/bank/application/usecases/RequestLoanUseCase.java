package com.bank.application.usecases;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.application.ports.AuditLogEntry;
import com.bank.application.ports.AuditLogRepositoryPort;
import com.bank.application.ports.ClientRepositoryPort;
import com.bank.application.ports.LoanRepositoryPort;
import com.bank.application.ports.SystemUserRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.UserStatus;
import com.bank.domain.entities.Loan;
import com.bank.domain.entities.LoanType;

@Service
public class RequestLoanUseCase {

    private final LoanRepositoryPort loanRepository;
    private final ClientRepositoryPort clientRepository;
    private final SystemUserRepositoryPort systemUserRepository;
    private final AuditLogRepositoryPort auditLogRepository;
    private final AuthContextService authContextService;

    public RequestLoanUseCase(LoanRepositoryPort loanRepository,
                                    ClientRepositoryPort clientRepository,
                                    SystemUserRepositoryPort systemUserRepository,
                                    AuditLogRepositoryPort auditLogRepository,
                                    AuthContextService authContextService) {
        this.loanRepository = loanRepository;
        this.clientRepository = clientRepository;
        this.systemUserRepository = systemUserRepository;
        this.auditLogRepository = auditLogRepository;
        this.authContextService = authContextService;
    }

    @Transactional
    public Loan execute(LoanType typeLoan,
                            String applicantClientId,
                            BigDecimal requestedAmount,
                            BigDecimal interestRate,
                            int termMonths) {
        if (!authContextService.hasAnyRole("NATURAL_CLIENT", "BUSINESS_CLIENT", "SALES")) {
            throw new SecurityException("Not authorized to request loans");
        }

        String clientRelated = authContextService.currentRelatedClientIdOrThrow();
        if (!clientRelated.equals(applicantClientId)) {
            throw new SecurityException("Not authorized to request loans for another client");
        }

        var clientApplicant = clientRepository.findById(applicantClientId)
                .orElseThrow(() -> new IllegalArgumentException("Applicant client not found"));

        var systemUser = systemUserRepository.findByIdIdentification(clientApplicant.getIdIdentification())
            .orElseThrow(() -> new IllegalStateException("No system user is associated with the client applicant"));
        if (systemUser.getUserStatus() != UserStatus.ACTIVE) {
            throw new IllegalStateException("Applicant client must be active");
        }

        Loan loan = new Loan(
                typeLoan,
                applicantClientId,
            clientApplicant.getIdIdentification(),
                requestedAmount,
                interestRate,
                termMonths
        );

        Loan saved = loanRepository.save(loan);

        auditLogRepository.save(new AuditLogEntry(
                UUID.randomUUID().toString(),
                "LOAN_REQUEST",
                LocalDateTime.now(),
                authContextService.currentUserId(),
                authContextService.currentRole(),
                saved.getId(),
                Map.of(
                    "creatorUserId", authContextService.currentUserId(),
                    "creationDate", LocalDateTime.now().toString(),
                    "idClientApplicant", saved.getApplicantClientId(),
                    "idClientApplicantIdentification", saved.getApplicantClientIdentification(),
                        "status", saved.getStatus().name(),
                        "requestedAmount", saved.getRequestedAmount(),
                        "typeLoan", saved.getLoanType().name()
                )
        ));

        return saved;
    }
}
