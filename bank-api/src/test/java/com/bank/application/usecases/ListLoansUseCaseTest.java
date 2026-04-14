package com.bank.application.usecases;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.bank.application.ports.LoanRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.LoanStatus;
import com.bank.domain.entities.Loan;
import com.bank.domain.entities.LoanType;

class ListLoansUseCaseTest {

    @Test
    void debeListTodoCuandoEsAnalyst() {
        FakeLoanRepositoryPort repo = new FakeLoanRepositoryPort();
        repo.storage.add(loan("p1", "client-1"));
        repo.storage.add(loan("p2", "client-2"));

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("analyst", "123456", "ROLE_ANALYST")
        );

        ListLoansUseCase useCase = new ListLoansUseCase(repo, new AuthContextService("client_natural:client-1"));

        List<Loan> resultado = useCase.execute();

        assertEquals(2, resultado.size());
    }

    @Test
    void debeListSoloLoansDelClientRelatedCuandoEsClient() {
        FakeLoanRepositoryPort repo = new FakeLoanRepositoryPort();
        repo.storage.add(loan("p1", "client-1"));
        repo.storage.add(loan("p2", "client-2"));

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("client_natural", "123456", "ROLE_NATURAL_CLIENT")
        );

        ListLoansUseCase useCase = new ListLoansUseCase(repo, new AuthContextService("client_natural:client-1"));

        List<Loan> resultado = useCase.execute();

        assertEquals(1, resultado.size());
        assertEquals("client-1", resultado.getFirst().getApplicantClientId());
    }

    @Test
    void salesDebeListSoloLoansDeClientsBajoManagement() {
        FakeLoanRepositoryPort repo = new FakeLoanRepositoryPort();
        repo.storage.add(loan("p1", "client-1"));
        repo.storage.add(loan("p2", "client-2"));

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("sales", "123456", "ROLE_SALES")
        );

        ListLoansUseCase useCase = new ListLoansUseCase(repo, new AuthContextService("sales:client-1"));

        List<Loan> resultado = useCase.execute();

        assertEquals(1, resultado.size());
        assertEquals("client-1", resultado.getFirst().getApplicantClientId());
    }

    @Test
    void tellerNoDebePoderGetLoans() {
        FakeLoanRepositoryPort repo = new FakeLoanRepositoryPort();
        repo.storage.add(loan("p1", "client-1"));

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("teller", "123456", "ROLE_TELLER")
        );

        ListLoansUseCase useCase = new ListLoansUseCase(repo, new AuthContextService(""));

        SecurityException thrown = assertThrows(SecurityException.class, useCase::execute);
        assertEquals("Not authorized to get loans", thrown.getMessage());
    }

    private Loan loan(String id, String clientId) {
        return new Loan(
                id,
                LoanType.CONSUMER,
                clientId,
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(800),
                BigDecimal.valueOf(1.5),
                12,
                LoanStatus.UNDER_REVIEW,
                null,
                null,
                null
        );
    }

    private static final class FakeLoanRepositoryPort implements LoanRepositoryPort {

        private final List<Loan> storage = new ArrayList<>();

        @Override
        public Loan save(Loan loan) {
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
}

