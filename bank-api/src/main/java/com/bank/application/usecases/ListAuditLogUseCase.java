package com.bank.application.usecases;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.bank.application.ports.AccountRepositoryPort;
import com.bank.application.ports.AuditLogEntry;
import com.bank.application.ports.AuditLogRepositoryPort;
import com.bank.application.ports.LoanRepositoryPort;
import com.bank.application.ports.TransactionRepositoryPort;
import com.bank.application.services.AuthContextService;

@Service
public class ListAuditLogUseCase {

    private final AuditLogRepositoryPort auditLogRepository;
    private final AccountRepositoryPort accountRepository;
    private final LoanRepositoryPort loanRepository;
    private final TransactionRepositoryPort transactionRepository;
    private final AuthContextService authContextService;

    public ListAuditLogUseCase(AuditLogRepositoryPort auditLogRepository,
                                 AccountRepositoryPort accountRepository,
                                 LoanRepositoryPort loanRepository,
                                 TransactionRepositoryPort transactionRepository,
                                 AuthContextService authContextService) {
        this.auditLogRepository = auditLogRepository;
        this.accountRepository = accountRepository;
        this.loanRepository = loanRepository;
        this.transactionRepository = transactionRepository;
        this.authContextService = authContextService;
    }

    public List<AuditLogEntry> execute(String userId) {
        return execute(userId, null);
    }

    public List<AuditLogEntry> execute(String userId, String affectedProductId) {
        if (!authContextService.hasRole("ANALYST")) {
            if (!authContextService.hasAnyRole("NATURAL_CLIENT", "BUSINESS_CLIENT", "COMPANY_EMPLOYEE", "COMPANY_SUPERVISOR")) {
                throw new SecurityException("Not authorized to get audit log");
            }

                Set<String> ownProductIds = ownProductIdsForClient();
                if (affectedProductId != null && !affectedProductId.isBlank() && !ownProductIds.contains(affectedProductId)) {
                throw new SecurityException("Not authorized to access audit logs for unrelated products");
            }

                List<String> filteredProductIds = (affectedProductId == null || affectedProductId.isBlank())
                    ? List.copyOf(ownProductIds)
                    : List.of(affectedProductId);
                return auditLogRepository.findByAffectedProductIdIn(filteredProductIds);
        }

        if (affectedProductId != null && !affectedProductId.isBlank()) {
            return auditLogRepository.findByAffectedProductIdIn(List.of(affectedProductId));
        }
        if (userId == null || userId.isBlank()) {
            return auditLogRepository.findAll();
        }
        return auditLogRepository.findByUserId(userId);
    }

    private Set<String> ownProductIdsForClient() {
        String clientId = authContextService.currentRelatedClientIdOrThrow();

        Set<String> ownProductIds = new HashSet<>();

        var clientAccounts = accountRepository.findByClientId(clientId);
        clientAccounts.forEach(account -> ownProductIds.add(account.getId()));

        loanRepository.findByClientApplicantId(clientId)
                .forEach(loan -> ownProductIds.add(loan.getId()));

        List<String> clientAccountNumbers = clientAccounts.stream()
                .map(account -> account.getAccountNumber().value())
                .toList();

        if (!clientAccountNumbers.isEmpty()) {
            transactionRepository.findByAccountSourceInOrAccountDestinationIn(clientAccountNumbers, clientAccountNumbers)
                    .forEach(transaction -> ownProductIds.add(transaction.getId()));
        }

        return ownProductIds;
    }
}

