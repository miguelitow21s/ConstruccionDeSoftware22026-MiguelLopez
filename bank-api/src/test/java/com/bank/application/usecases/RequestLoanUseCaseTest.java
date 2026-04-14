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

import com.bank.application.ports.AuditLogEntry;
import com.bank.application.ports.AuditLogRepositoryPort;
import com.bank.application.ports.ClientRepositoryPort;
import com.bank.application.ports.LoanRepositoryPort;
import com.bank.application.ports.SystemUserRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.Client;
import com.bank.domain.entities.UserStatus;
import com.bank.domain.entities.Loan;
import com.bank.domain.entities.SystemRole;
import com.bank.domain.entities.LoanType;
import com.bank.domain.entities.SystemUser;
import com.bank.domain.valueobjects.Email;

class RequestLoanUseCaseTest {

    @Test
    void clientNaturalNoPuedeRequestLoanParaOtroClient() {
        FakeClientRepository clientRepo = new FakeClientRepository();
        clientRepo.storage.add(new Client("id-1", "10101010", "Client Uno", new Email("uno@bank.com"), "3001111111"));

        RequestLoanUseCase useCase = new RequestLoanUseCase(
                new FakeLoanRepository(),
                clientRepo,
            userRepoConActivo("id-1", "10101010"),
                new FakeAuditLogRepository(),
                new AuthContextService("client_natural:id-1")
        );

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("client_natural", "123456", "ROLE_NATURAL_CLIENT")
        );

        SecurityException thrown = assertThrows(
                SecurityException.class,
                () -> useCase.execute(LoanType.CONSUMER, "id-otro", BigDecimal.valueOf(1000), BigDecimal.valueOf(1.2), 12)
        );
        assertEquals("Not authorized to request loans for another client", thrown.getMessage());
    }

    @Test
    void salesPuedeRequestLoanParaClientExistente() {
        FakeClientRepository clientRepo = new FakeClientRepository();
        clientRepo.storage.add(new Client("id-1", "10101010", "Client Uno", new Email("uno@bank.com"), "3001111111"));

        FakeLoanRepository loanRepo = new FakeLoanRepository();
        RequestLoanUseCase useCase = new RequestLoanUseCase(
                loanRepo,
                clientRepo,
                userRepoConActivo("id-1", "10101010"),
                new FakeAuditLogRepository(),
            new AuthContextService("sales:id-1")
        );

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("sales", "123456", "ROLE_SALES")
        );

        Loan loan = useCase.execute(LoanType.CONSUMER, "id-1", BigDecimal.valueOf(2000), BigDecimal.valueOf(1.5), 18);

        assertEquals("id-1", loan.getApplicantClientId());
        assertEquals(1, loanRepo.storage.size());
    }

        @Test
        void salesNoPuedeRequestLoanParaClientFueraDeSuManagement() {
        FakeClientRepository clientRepo = new FakeClientRepository();
        clientRepo.storage.add(new Client("id-1", "10101010", "Client Uno", new Email("uno@bank.com"), "3001111111"));
        clientRepo.storage.add(new Client("id-2", "20202020", "Client Dos", new Email("dos@bank.com"), "3002222222"));

        RequestLoanUseCase useCase = new RequestLoanUseCase(
            new FakeLoanRepository(),
            clientRepo,
            userRepoConActivo("id-1", "10101010"),
            new FakeAuditLogRepository(),
            new AuthContextService("sales:id-1")
        );

        SecurityContextHolder.getContext().setAuthentication(
            new TestingAuthenticationToken("sales", "123456", "ROLE_SALES")
        );

        SecurityException thrown = assertThrows(
            SecurityException.class,
            () -> useCase.execute(LoanType.CONSUMER, "id-2", BigDecimal.valueOf(1200), BigDecimal.valueOf(1.3), 12)
        );
        assertEquals("Not authorized to request loans for another client", thrown.getMessage());
        }

    @Test
    void empleadoCompanyNoPuedeRequestLoans() {
        FakeClientRepository clientRepo = new FakeClientRepository();
        clientRepo.storage.add(new Client("id-company", "30303030", "Company Uno", new Email("company@bank.com"), "3003333333"));

        RequestLoanUseCase useCase = new RequestLoanUseCase(
                new FakeLoanRepository(),
                clientRepo,
            userRepoConActivo("id-company", "30303030"),
                new FakeAuditLogRepository(),
                new AuthContextService("empleado_company:id-company")
        );

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("empleado_company", "123456", "ROLE_COMPANY_EMPLOYEE")
        );

        SecurityException thrown = assertThrows(
                SecurityException.class,
                () -> useCase.execute(LoanType.CONSUMER, "id-company", BigDecimal.valueOf(1800), BigDecimal.valueOf(1.4), 12)
        );
        assertEquals("Not authorized to request loans", thrown.getMessage());
    }

        @Test
        void noDebePermitirSolicitudSiClientNoEstaActivo() {
        FakeClientRepository clientRepo = new FakeClientRepository();
        clientRepo.storage.add(new Client("id-1", "10101010", "Client Uno", new Email("uno@bank.com"), "3001111111"));

        FakeSystemUserRepository userRepo = new FakeSystemUserRepository();
        userRepo.storage.add(new SystemUser(
            1L,
            "id-1",
            "Client Uno",
            "10101010",
            new Email("uno@bank.com"),
            "3001111111",
            java.time.LocalDate.of(1990, 1, 1),
            "Calle 1",
            SystemRole.NATURAL_PERSON_CLIENT,
            UserStatus.INACTIVE
        ));

        RequestLoanUseCase useCase = new RequestLoanUseCase(
            new FakeLoanRepository(),
            clientRepo,
            userRepo,
            new FakeAuditLogRepository(),
            new AuthContextService("client_natural:id-1")
        );

        SecurityContextHolder.getContext().setAuthentication(
            new TestingAuthenticationToken("client_natural", "123456", "ROLE_NATURAL_CLIENT")
        );

        IllegalStateException thrown = assertThrows(
            IllegalStateException.class,
            () -> useCase.execute(LoanType.CONSUMER, "id-1", BigDecimal.valueOf(1000), BigDecimal.valueOf(1.2), 12)
        );
        assertEquals("Applicant client must be active", thrown.getMessage());
        }

        private static FakeSystemUserRepository userRepoConActivo(String idRelated, String identification) {
        FakeSystemUserRepository repo = new FakeSystemUserRepository();
        repo.storage.add(new SystemUser(
            Math.abs(idRelated.hashCode()) + 1L,
            idRelated,
            "Client",
            identification,
            new Email("client-" + identification + "@bank.com"),
            "3001234567",
            java.time.LocalDate.of(1990, 1, 1),
            "Calle 123",
            SystemRole.NATURAL_PERSON_CLIENT,
            UserStatus.ACTIVE
        ));
        return repo;
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

    private static final class FakeClientRepository implements ClientRepositoryPort {
        private final List<Client> storage = new ArrayList<>();

        @Override
        public Client save(Client client) {
            storage.removeIf(existing -> existing.getId().equals(client.getId()));
            storage.add(client);
            return client;
        }

        @Override
        public Optional<Client> findById(String id) {
            return storage.stream().filter(c -> c.getId().equals(id)).findFirst();
        }

        @Override
        public Optional<Client> findByEmail(String email) {
            return storage.stream().filter(c -> c.getEmail().value().equalsIgnoreCase(email)).findFirst();
        }

        @Override
        public Optional<Client> findByIdIdentification(String identificationId) {
            return storage.stream().filter(c -> c.getIdIdentification().equals(identificationId)).findFirst();
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

    private static final class FakeSystemUserRepository implements SystemUserRepositoryPort {
        private final List<SystemUser> storage = new ArrayList<>();

        @Override
        public SystemUser save(SystemUser systemUser) {
            storage.removeIf(existing -> existing.getUserId().equals(systemUser.getUserId()));
            storage.add(systemUser);
            return systemUser;
        }

        @Override
        public Optional<SystemUser> findByUserId(Long userId) {
            return storage.stream().filter(u -> u.getUserId().equals(userId)).findFirst();
        }

        @Override
        public Optional<SystemUser> findByIdIdentification(String identificationId) {
            return storage.stream().filter(u -> u.getIdIdentification().equals(identificationId)).findFirst();
        }
    }
}

