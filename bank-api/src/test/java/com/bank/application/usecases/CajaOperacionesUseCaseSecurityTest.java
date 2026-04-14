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

import com.bank.application.ports.AccountRepositoryPort;
import com.bank.application.ports.ClientRepositoryPort;
import com.bank.application.ports.TransactionRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.Account;
import com.bank.domain.entities.AccountStatus;
import com.bank.domain.entities.AccountType;
import com.bank.domain.entities.Client;
import com.bank.domain.entities.ClientType;
import com.bank.domain.entities.Transaction;
import com.bank.domain.entities.TransactionStatus;
import com.bank.domain.services.AccountService;
import com.bank.domain.valueobjects.AccountNumber;
import com.bank.domain.valueobjects.Email;
import com.bank.domain.valueobjects.Money;

class CajaOperacionesUseCaseSecurityTest {

    @Test
    void salesNoPuedeDepositarNiRetirarDirectamente() {
        FakeAccountRepository accountRepo = new FakeAccountRepository();
        FakeClientRepository clientRepo = new FakeClientRepository();
        accountRepo.storage.add(account("c1", "10000031", "id-1", BigDecimal.valueOf(1000)));
        clientRepo.storage.add(client("id-1", "12345678"));

        DepositMoneyUseCase deposit = new DepositMoneyUseCase(
                accountRepo,
                clientRepo,
                new FakeTransactionRepository(),
                new AccountService(),
                new AuthContextService("sales:id-1")
        );

        WithdrawMoneyUseCase withdraw = new WithdrawMoneyUseCase(
                accountRepo,
                clientRepo,
                new FakeTransactionRepository(),
                new AccountService(),
                new AuthContextService("sales:id-1")
        );

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("sales", "123456", "ROLE_SALES")
        );

        SecurityException dep = assertThrows(SecurityException.class, () -> deposit.execute("c1", "12345678", BigDecimal.valueOf(10)));
        SecurityException ret = assertThrows(SecurityException.class, () -> withdraw.execute("c1", "12345678", BigDecimal.valueOf(10)));

        assertEquals("Not authorized to perform deposits", dep.getMessage());
        assertEquals("Not authorized to perform withdrawals", ret.getMessage());
    }

        @Test
        void analystNoPuedeDepositarNiRetirarDirectamente() {
        FakeAccountRepository accountRepo = new FakeAccountRepository();
        FakeClientRepository clientRepo = new FakeClientRepository();
        accountRepo.storage.add(account("c2", "10000032", "id-2", BigDecimal.valueOf(500)));
        clientRepo.storage.add(client("id-2", "87654321"));

        DepositMoneyUseCase deposit = new DepositMoneyUseCase(
            accountRepo,
            clientRepo,
            new FakeTransactionRepository(),
            new AccountService(),
            new AuthContextService("")
        );

        WithdrawMoneyUseCase withdraw = new WithdrawMoneyUseCase(
            accountRepo,
            clientRepo,
            new FakeTransactionRepository(),
            new AccountService(),
            new AuthContextService("")
        );

        SecurityContextHolder.getContext().setAuthentication(
            new TestingAuthenticationToken("analyst", "123456", "ROLE_ANALYST")
        );

        SecurityException dep = assertThrows(SecurityException.class, () -> deposit.execute("c2", "87654321", BigDecimal.valueOf(10)));
        SecurityException ret = assertThrows(SecurityException.class, () -> withdraw.execute("c2", "87654321", BigDecimal.valueOf(10)));

        assertEquals("Not authorized to perform deposits", dep.getMessage());
        assertEquals("Not authorized to perform withdrawals", ret.getMessage());
        }

    private Account account(String id, String number, String clientId, BigDecimal balance) {
        return new Account(id, new AccountNumber(number), new Money(balance), AccountType.SAVINGS, clientId, AccountStatus.ACTIVE);
    }

    private Client client(String id, String identificationId) {
        return new Client(
                id,
                identificationId,
                "Client",
            new Email("client@bank.com"),
                "11111111",
                ClientType.NATURAL_PERSON_CLIENT,
                null
        );
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
            return storage.stream().filter(c -> c.getEmail().value().equals(email)).findFirst();
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

