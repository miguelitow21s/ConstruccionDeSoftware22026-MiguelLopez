package com.bank.application.usecases;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
import com.bank.application.ports.TransactionRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.Client;
import com.bank.domain.entities.Account;
import com.bank.domain.entities.AccountStatus;
import com.bank.domain.entities.TransactionStatus;
import com.bank.domain.entities.ClientType;
import com.bank.domain.entities.AccountType;
import com.bank.domain.entities.Transaction;
import com.bank.domain.services.AccountService;
import com.bank.domain.valueobjects.Money;
import com.bank.domain.valueobjects.Email;
import com.bank.domain.valueobjects.AccountNumber;

class DepositarWithdrawMoneyUseCaseTest {

    @Test
    void depositDebeValidateIdentificationClient() {
        FakeAccountRepository accountRepo = new FakeAccountRepository();
        accountRepo.storage.add(account("c1", "10000201", "client-1", BigDecimal.valueOf(500)));
        FakeClientRepository clientRepo = new FakeClientRepository();
        clientRepo.storage.add(client("client-1", "10101010"));

        DepositMoneyUseCase useCase = new DepositMoneyUseCase(
                accountRepo,
                clientRepo,
                new FakeTransactionRepository(),
                new AccountService(),
                new AuthContextService("")
        );

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("teller", "123456", "ROLE_TELLER")
        );

        SecurityException thrown = assertThrows(
                SecurityException.class,
                () -> useCase.execute("c1", "99999999", BigDecimal.valueOf(100))
        );
        assertEquals("Client identification does not match the account", thrown.getMessage());
    }

    @Test
    void withdrawDebeValidateIdentificationClient() {
        FakeAccountRepository accountRepo = new FakeAccountRepository();
        accountRepo.storage.add(account("c2", "10000202", "client-2", BigDecimal.valueOf(800)));
        FakeClientRepository clientRepo = new FakeClientRepository();
        clientRepo.storage.add(client("client-2", "20202020"));

        WithdrawMoneyUseCase useCase = new WithdrawMoneyUseCase(
                accountRepo,
                clientRepo,
                new FakeTransactionRepository(),
                new AccountService(),
                new AuthContextService("")
        );

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("teller", "123456", "ROLE_TELLER")
        );

        SecurityException thrown = assertThrows(
                SecurityException.class,
                () -> useCase.execute("c2", "11111111", BigDecimal.valueOf(50))
        );
        assertEquals("Client identification does not match the account", thrown.getMessage());
    }

    private Account account(String id, String accountNumber, String clientId, BigDecimal balance) {
        return new Account(id, new AccountNumber(accountNumber), new Money(balance), AccountType.SAVINGS, clientId, AccountStatus.ACTIVE);
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

    private static final class FakeTransactionRepository implements TransactionRepositoryPort {
        private final List<Transaction> storage = new ArrayList<>();

        @Override
        public Transaction save(Transaction transaction) {
            storage.removeIf(existing -> existing.getId().equals(transaction.getId()));
            storage.add(transaction);
            return transaction;
        }

        @Override
        public Optional<Transaction> findById(String id) {
            return storage.stream().filter(t -> t.getId().equals(id)).findFirst();
        }

        @Override
        public List<Transaction> findAll() {
            return List.copyOf(storage);
        }

        @Override
        public List<Transaction> findByAccountSourceInOrAccountDestinationIn(List<String> accountsSource, List<String> accountsDestination) {
            return storage.stream()
                    .filter(t -> accountsSource.contains(t.getSourceAccount()) || accountsDestination.contains(t.getDestinationAccount()))
                    .toList();
        }

        @Override
        public List<Transaction> findByStatusAndDateBefore(TransactionStatus status, LocalDateTime date) {
            return storage.stream().filter(t -> t.getStatus() == status && t.getDate().isBefore(date)).toList();
        }
    }
}

