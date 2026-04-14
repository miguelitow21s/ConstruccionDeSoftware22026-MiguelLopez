package com.bank.application.usecases;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.bank.application.ports.AuditLogEntry;
import com.bank.application.ports.AuditLogRepositoryPort;
import com.bank.application.ports.LoanRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.LoanStatus;
import com.bank.domain.entities.Loan;
import com.bank.domain.entities.LoanType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DecisionLoanUseCasesSecurityTest {

    @Test
    void analystPuedeAprobarYRejectLoans() {
        FakeLoanRepository repo = new FakeLoanRepository();
        repo.storage.add(loanEnEstudio("p1"));
        repo.storage.add(loanEnEstudio("p2"));

        FakeAuditLogRepository auditLogRepo = new FakeAuditLogRepository();
        ApproveLoanUseCase approveUseCase = new ApproveLoanUseCase(repo, auditLogRepo, new AuthContextService(""));
        RejectLoanUseCase rejectUseCase = new RejectLoanUseCase(repo, auditLogRepo, new AuthContextService(""));

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("analyst", "123456", "ROLE_ANALYST")
        );

        Loan aprobado = approveUseCase.execute("p1", BigDecimal.valueOf(900));
        Loan rechazado = rejectUseCase.execute("p2");

        assertEquals(LoanStatus.APPROVED, aprobado.getStatus());
        assertEquals(LoanStatus.REJECTED, rechazado.getStatus());
        assertEquals(2, auditLogRepo.storage.size());
    }

    @Test
    void noAnalystNoPuedeAprobarNiRejectLoans() {
        FakeLoanRepository repo = new FakeLoanRepository();
        repo.storage.add(loanEnEstudio("p3"));

        ApproveLoanUseCase approveUseCase = new ApproveLoanUseCase(repo, new FakeAuditLogRepository(), new AuthContextService(""));
        RejectLoanUseCase rejectUseCase = new RejectLoanUseCase(repo, new FakeAuditLogRepository(), new AuthContextService(""));

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("sales", "123456", "ROLE_SALES")
        );

        SecurityException approveError = assertThrows(SecurityException.class,
                () -> approveUseCase.execute("p3", BigDecimal.valueOf(800)));
        SecurityException rejectError = assertThrows(SecurityException.class,
                () -> rejectUseCase.execute("p3"));

        assertEquals("Not authorized to approve loans", approveError.getMessage());
        assertEquals("Not authorized to reject loans", rejectError.getMessage());
    }

    private Loan loanEnEstudio(String id) {
        return new Loan(
                id,
                LoanType.CONSUMER,
                "client-1",
                BigDecimal.valueOf(1000),
                null,
                BigDecimal.valueOf(1.5),
                12,
                LoanStatus.UNDER_REVIEW,
                null,
                null,
                null
        );
    }

    private static final class FakeLoanRepository implements LoanRepositoryPort {
        private final List<Loan> storage = new ArrayList<>();

        @Override
        public Loan save(Loan loan) {
            storage.removeIf(existing -> existing.getId().equals(loan.getId()));
            storage.add(loan);
            return loan;
        }

        @Override
        public Optional<Loan> findById(String id) {
            return storage.stream().filter(p -> p.getId().equals(id)).findFirst();
        }

        @Override
        public List<Loan> findAll() {
            return List.copyOf(storage);
        }

        @Override
        public List<Loan> findByClientSolicitanteId(String applicantClientId) {
            return storage.stream().filter(p -> p.getApplicantClientId().equals(applicantClientId)).toList();
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

