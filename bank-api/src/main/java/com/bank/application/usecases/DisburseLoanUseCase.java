package com.bank.application.usecases;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.application.ports.AuditLogEntry;
import com.bank.application.ports.AuditLogRepositoryPort;
import com.bank.application.ports.AccountRepositoryPort;
import com.bank.application.ports.LoanRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.Account;
import com.bank.domain.entities.Loan;
import com.bank.domain.valueobjects.Money;

@Service
public class DisburseLoanUseCase {

    private final LoanRepositoryPort loanRepository;
    private final AccountRepositoryPort accountRepository;
    private final AuditLogRepositoryPort auditLogRepository;
    private final AuthContextService authContextService;

    public DisburseLoanUseCase(LoanRepositoryPort loanRepository,
                                      AccountRepositoryPort accountRepository,
                                      AuditLogRepositoryPort auditLogRepository,
                                      AuthContextService authContextService) {
        this.loanRepository = loanRepository;
        this.accountRepository = accountRepository;
        this.auditLogRepository = auditLogRepository;
        this.authContextService = authContextService;
    }

    @Transactional
    public Loan execute(String loanId, String accountNumberDestination) {
                if (!authContextService.hasRole("ANALYST")) {
                        throw new SecurityException("Not authorized to disburse loans");
                }

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found"));

        Account destinationAccount = accountRepository.findByAccountNumber(accountNumberDestination)
                .orElseThrow(() -> new IllegalArgumentException("Destination account not found"));

        if (!destinationAccount.getClientId().equals(loan.getApplicantClientId())) {
            throw new IllegalStateException("Destination account does not belong to the loan client");
        }

        destinationAccount.validateOperationalAccount();
        loan.disburse(accountNumberDestination);

        destinationAccount.deposit(Money.positive(loan.getApprovedAmount()));
        accountRepository.save(destinationAccount);
        Loan saved = loanRepository.save(loan);

        auditLogRepository.save(new AuditLogEntry(
                UUID.randomUUID().toString(),
                "Disbursement_Loan",
                LocalDateTime.now(),
                authContextService.currentUserId(),
                authContextService.currentRole(),
                saved.getId(),
                Map.of(
                        "userIdDisbursement", authContextService.currentUserId(),
                        "approvalDate", saved.getApprovalDate() != null ? saved.getApprovalDate().toString() : "",
                        "disbursementDate", saved.getDisbursementDate() != null ? saved.getDisbursementDate().toString() : LocalDateTime.now().toString(),
                        "accountNumberDestination", accountNumberDestination,
                        "disbursedAmount", saved.getApprovedAmount(),
                        "newStatus", saved.getStatus().name()
                )
        ));

        return saved;
    }
}
