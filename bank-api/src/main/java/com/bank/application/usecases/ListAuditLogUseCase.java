package com.bank.application.usecases;

import com.bank.application.ports.AuditLogEntry;
import com.bank.application.ports.AuditLogRepositoryPort;
import com.bank.application.ports.AccountRepositoryPort;
import com.bank.application.ports.LoanRepositoryPort;
import com.bank.application.ports.TransactionRepositoryPort;
import com.bank.application.services.AuthContextService;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public List<AuditLogEntry> execute(String userId, String idProductoAfectado) {
        if (!authContextService.hasRole("ANALYST")) {
            if (!authContextService.hasAnyRole("NATURAL_CLIENT", "BUSINESS_CLIENT", "COMPANY_EMPLOYEE", "COMPANY_SUPERVISOR")) {
                throw new SecurityException("Not authorized to get audit log");
            }

            Set<String> productosPropios = idsProductoPropiosDelClient();
            if (idProductoAfectado != null && !idProductoAfectado.isBlank() && !productosPropios.contains(idProductoAfectado)) {
                throw new SecurityException("Not authorized to access audit logs for unrelated products");
            }

            List<String> filtroProductos = (idProductoAfectado == null || idProductoAfectado.isBlank())
                    ? List.copyOf(productosPropios)
                    : List.of(idProductoAfectado);
            return auditLogRepository.findByIdProductoAfectadoIn(filtroProductos);
        }

        if (idProductoAfectado != null && !idProductoAfectado.isBlank()) {
            return auditLogRepository.findByIdProductoAfectadoIn(List.of(idProductoAfectado));
        }
        if (userId == null || userId.isBlank()) {
            return auditLogRepository.findAll();
        }
        return auditLogRepository.findByUserId(userId);
    }

    private Set<String> idsProductoPropiosDelClient() {
        String clientId = authContextService.currentRelatedClientIdOrThrow();

        Set<String> idsProducto = new HashSet<>();

        var accountsClient = accountRepository.findByClientId(clientId);
        accountsClient.forEach(account -> idsProducto.add(account.getId()));

        loanRepository.findByClientApplicantId(clientId)
                .forEach(loan -> idsProducto.add(loan.getId()));

        List<String> numbersAccountClient = accountsClient.stream()
                .map(account -> account.getAccountNumber().value())
                .toList();

        if (!numbersAccountClient.isEmpty()) {
            transactionRepository.findByAccountSourceInOrAccountDestinationIn(numbersAccountClient, numbersAccountClient)
                    .forEach(transaction -> idsProducto.add(transaction.getId()));
        }

        return idsProducto;
    }
}
