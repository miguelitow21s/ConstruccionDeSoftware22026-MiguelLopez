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

import com.bank.application.ports.ClientRepositoryPort;
import com.bank.application.ports.AccountRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.Client;
import com.bank.domain.entities.Account;
import com.bank.domain.entities.AccountStatus;
import com.bank.domain.entities.ClientType;
import com.bank.domain.entities.AccountType;
import com.bank.domain.services.AccountService;
import com.bank.domain.valueobjects.Money;
import com.bank.domain.valueobjects.Email;
import com.bank.domain.valueobjects.AccountNumber;

class GetBalanceUseCaseTest {

    @Test
    void clientNaturalSoloPuedeGetSuPropiaAccount() {
        FakeAccountRepository accountRepo = new FakeAccountRepository();
        accountRepo.storage.add(account("c1", "10000011", "client-1", BigDecimal.valueOf(500)));
        FakeClientRepository clientRepo = new FakeClientRepository();
        clientRepo.storage.add(client("client-1", "10101010"));

        GetBalanceUseCase useCase = new GetBalanceUseCase(
            accountRepo,
            clientRepo,
                new AccountService(),
                new AuthContextService("client_natural:client-1")
        );

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("client_natural", "123456", "ROLE_NATURAL_CLIENT")
        );

        Money balance = useCase.execute("c1", null);
        assertEquals(BigDecimal.valueOf(500).setScale(2), balance.value());
    }

    @Test
    void clientNaturalNoPuedeGetAccountDeOtroClient() {
        FakeAccountRepository accountRepo = new FakeAccountRepository();
        accountRepo.storage.add(account("c2", "10000012", "client-2", BigDecimal.valueOf(800)));
        FakeClientRepository clientRepo = new FakeClientRepository();
        clientRepo.storage.add(client("client-2", "20202020"));

        GetBalanceUseCase useCase = new GetBalanceUseCase(
            accountRepo,
            clientRepo,
                new AccountService(),
                new AuthContextService("client_natural:client-1")
        );

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("client_natural", "123456", "ROLE_NATURAL_CLIENT")
        );

        SecurityException thrown = assertThrows(SecurityException.class, () -> useCase.execute("c2", null));
        assertEquals("Not authorized to access this account", thrown.getMessage());
    }

        @Test
        void tellerDebeValidateIdentificationClientParaGetBalance() {
        FakeAccountRepository accountRepo = new FakeAccountRepository();
        accountRepo.storage.add(account("c3", "10000013", "client-3", BigDecimal.valueOf(1200)));
        FakeClientRepository clientRepo = new FakeClientRepository();
        clientRepo.storage.add(client("client-3", "30303030"));

        GetBalanceUseCase useCase = new GetBalanceUseCase(
            accountRepo,
            clientRepo,
            new AccountService(),
            new AuthContextService("")
        );

        SecurityContextHolder.getContext().setAuthentication(
            new TestingAuthenticationToken("teller", "123456", "ROLE_TELLER")
        );

        Money balance = useCase.execute("c3", "30303030");
        assertEquals(BigDecimal.valueOf(1200).setScale(2), balance.value());
        }

        @Test
        void tellerDebeFallarSiIdentificationNoCoincide() {
        FakeAccountRepository accountRepo = new FakeAccountRepository();
        accountRepo.storage.add(account("c4", "10000014", "client-4", BigDecimal.valueOf(1500)));
        FakeClientRepository clientRepo = new FakeClientRepository();
        clientRepo.storage.add(client("client-4", "40404040"));

        GetBalanceUseCase useCase = new GetBalanceUseCase(
            accountRepo,
            clientRepo,
            new AccountService(),
            new AuthContextService("")
        );

        SecurityContextHolder.getContext().setAuthentication(
            new TestingAuthenticationToken("teller", "123456", "ROLE_TELLER")
        );

        SecurityException thrown = assertThrows(SecurityException.class, () -> useCase.execute("c4", "00000000"));
        assertEquals("Client identification does not match the account", thrown.getMessage());
        }

        @Test
        void salesSoloPuedeGetBalanceDeClientBajoManagement() {
        FakeAccountRepository accountRepo = new FakeAccountRepository();
        accountRepo.storage.add(account("c5", "10000015", "client-5", BigDecimal.valueOf(2100)));
        FakeClientRepository clientRepo = new FakeClientRepository();
        clientRepo.storage.add(client("client-5", "50505050"));

        GetBalanceUseCase useCase = new GetBalanceUseCase(
            accountRepo,
            clientRepo,
            new AccountService(),
            new AuthContextService("sales:client-5")
        );

        SecurityContextHolder.getContext().setAuthentication(
            new TestingAuthenticationToken("sales", "123456", "ROLE_SALES")
        );

        Money balance = useCase.execute("c5", null);
        assertEquals(BigDecimal.valueOf(2100).setScale(2), balance.value());
        }

        @Test
        void salesNoPuedeGetBalanceDeClientFueraDeManagement() {
        FakeAccountRepository accountRepo = new FakeAccountRepository();
        accountRepo.storage.add(account("c6", "10000016", "client-6", BigDecimal.valueOf(1800)));
        FakeClientRepository clientRepo = new FakeClientRepository();
        clientRepo.storage.add(client("client-6", "60606060"));

        GetBalanceUseCase useCase = new GetBalanceUseCase(
            accountRepo,
            clientRepo,
            new AccountService(),
            new AuthContextService("sales:client-otro")
        );

        SecurityContextHolder.getContext().setAuthentication(
            new TestingAuthenticationToken("sales", "123456", "ROLE_SALES")
        );

        SecurityException thrown = assertThrows(SecurityException.class, () -> useCase.execute("c6", null));
        assertEquals("Not authorized to access this account", thrown.getMessage());
        }

    private Account account(String id, String number, String clientId, BigDecimal balance) {
        return new Account(id, new AccountNumber(number), new Money(balance), AccountType.SAVINGS, clientId, AccountStatus.ACTIVE);
    }

    private Client client(String id, String identificationId) {
        return new Client(id, identificationId, "Client", new Email(identificationId + "@bank.com"), "3001234567", ClientType.NATURAL_PERSON_CLIENT, null);
    }

    private static final class FakeAccountRepository implements AccountRepositoryPort {
        private final List<Account> storage = new ArrayList<>();

        @Override
        public Account save(Account account) {
            storage.removeIf(existing -> existing.getId().equals(account.getId()));
            storage.add(account);
            return account;
        }

        @Override
        public Optional<Account> findById(String id) {
            return storage.stream().filter(c -> c.getId().equals(id)).findFirst();
        }

        @Override
        public Optional<Account> findByAccountNumber(String accountNumber) {
            return storage.stream().filter(c -> c.getAccountNumber().value().equals(accountNumber)).findFirst();
        }

        @Override
        public List<Account> findByClientId(String clientId) {
            return storage.stream().filter(c -> c.getClientId().equals(clientId)).toList();
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
}

