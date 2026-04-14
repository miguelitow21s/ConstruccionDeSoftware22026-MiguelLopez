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

import com.bank.application.ports.AccountRepositoryPort;
import com.bank.application.ports.BankingProductRepositoryPort;
import com.bank.application.ports.ClientRepositoryPort;
import com.bank.application.ports.SystemUserRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.Account;
import com.bank.domain.entities.AccountType;
import com.bank.domain.entities.BankingProduct;
import com.bank.domain.entities.Client;
import com.bank.domain.entities.ClientType;
import com.bank.domain.entities.ProductCategory;
import com.bank.domain.entities.SystemRole;
import com.bank.domain.entities.SystemUser;
import com.bank.domain.entities.UserStatus;
import com.bank.domain.valueobjects.Email;

class CreateAccountUseCaseTest {

    @Test
    void salesPuedeAbrirAccountParaClientBajoManagement() {
        FakeClientRepository clientRepo = new FakeClientRepository();
        clientRepo.storage.add(new Client("id-1", "10101010", "Client Uno", new Email("uno@bank.com"), "3001111111", ClientType.NATURAL_PERSON_CLIENT, null));
        FakeSystemUserRepository userRepo = new FakeSystemUserRepository();
        userRepo.storage.add(userActivo("id-1", "10101010"));

        CreateAccountUseCase useCase = new CreateAccountUseCase(
                new FakeAccountRepository(),
                clientRepo,
            userRepo,
            new FakeBankingProductRepository(),
                new AuthContextService("sales:id-1")
        );

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("sales", "123456", "ROLE_SALES")
        );

        Account account = useCase.execute("12345678", BigDecimal.valueOf(1000), AccountType.SAVINGS, "id-1");

        assertEquals("id-1", account.getClientId());
    }

    @Test
    void salesNoPuedeAbrirAccountParaClientFueraDeManagement() {
        FakeClientRepository clientRepo = new FakeClientRepository();
        clientRepo.storage.add(new Client("id-1", "10101010", "Client Uno", new Email("uno@bank.com"), "3001111111", ClientType.NATURAL_PERSON_CLIENT, null));
        clientRepo.storage.add(new Client("id-2", "20202020", "Client Dos", new Email("dos@bank.com"), "3002222222", ClientType.NATURAL_PERSON_CLIENT, null));
        FakeSystemUserRepository userRepo = new FakeSystemUserRepository();
        userRepo.storage.add(userActivo("id-1", "10101010"));
        userRepo.storage.add(userActivo("id-2", "20202020"));

        CreateAccountUseCase useCase = new CreateAccountUseCase(
                new FakeAccountRepository(),
                clientRepo,
            userRepo,
            new FakeBankingProductRepository(),
                new AuthContextService("sales:id-1")
        );

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("sales", "123456", "ROLE_SALES")
        );

        SecurityException thrown = assertThrows(
                SecurityException.class,
                () -> useCase.execute("12345679", BigDecimal.valueOf(1000), AccountType.SAVINGS, "id-2")
        );
        assertEquals("Not authorized to open accounts for clients outside their scope", thrown.getMessage());
    }

        @Test
        void noDebeAbrirAccountSiClientEstaInactiveOBloqueado() {
        FakeClientRepository clientRepo = new FakeClientRepository();
        clientRepo.storage.add(new Client("id-1", "10101010", "Client Uno", new Email("uno@bank.com"), "3001111111", ClientType.NATURAL_PERSON_CLIENT, null));

        FakeSystemUserRepository userRepo = new FakeSystemUserRepository();
        userRepo.storage.add(new SystemUser(
            10L,
            "id-1",
            "Client Uno",
            "10101010",
            new Email("uno@bank.com"),
            "3001111111",
            java.time.LocalDate.of(1990, 1, 1),
            "Calle 1",
            SystemRole.NATURAL_PERSON_CLIENT,
            UserStatus.BLOCKED
        ));

        CreateAccountUseCase useCase = new CreateAccountUseCase(
            new FakeAccountRepository(),
            clientRepo,
            userRepo,
            new FakeBankingProductRepository(),
            new AuthContextService("sales:id-1")
        );

        SecurityContextHolder.getContext().setAuthentication(
            new TestingAuthenticationToken("sales", "123456", "ROLE_SALES")
        );

        IllegalStateException thrown = assertThrows(
            IllegalStateException.class,
            () -> useCase.execute("12345680", BigDecimal.valueOf(1000), AccountType.SAVINGS, "id-1")
        );
        assertEquals("Cannot open an account for an inactive or blocked client", thrown.getMessage());
        }

        @Test
        void noDebeAbrirAccountConTypeNoRegistradoEnCatalogo() {
        FakeClientRepository clientRepo = new FakeClientRepository();
        clientRepo.storage.add(new Client("id-1", "10101010", "Client Uno", new Email("uno@bank.com"), "3001111111", ClientType.NATURAL_PERSON_CLIENT, null));

        FakeBankingProductRepository productoRepo = new FakeBankingProductRepository();
        productoRepo.storage.removeIf(producto -> producto.getProductCode().equals(AccountType.SAVINGS.name()));

        CreateAccountUseCase useCase = new CreateAccountUseCase(
            new FakeAccountRepository(),
            clientRepo,
            userRepoConActivo("id-1", "10101010"),
            productoRepo,
            new AuthContextService("sales:id-1")
        );

        SecurityContextHolder.getContext().setAuthentication(
            new TestingAuthenticationToken("sales", "123456", "ROLE_SALES")
        );

        IllegalArgumentException thrown = assertThrows(
            IllegalArgumentException.class,
            () -> useCase.execute("12345681", BigDecimal.valueOf(1000), AccountType.SAVINGS, "id-1")
        );
        assertEquals("Account type does not exist in the banking catalog", thrown.getMessage());
        }

        private static FakeSystemUserRepository userRepoConActivo(String idRelated, String identification) {
        FakeSystemUserRepository repo = new FakeSystemUserRepository();
        repo.storage.add(userActivo(idRelated, identification));
        return repo;
        }

        private static SystemUser userActivo(String idRelated, String identification) {
        return new SystemUser(
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
            return storage.stream().filter(c -> c.getEmail().value().equalsIgnoreCase(email)).findFirst();
        }

        @Override
        public Optional<Client> findByIdIdentification(String identificationId) {
            return storage.stream().filter(c -> c.getIdIdentification().equals(identificationId)).findFirst();
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

    private static final class FakeBankingProductRepository implements BankingProductRepositoryPort {
        private final List<BankingProduct> storage = new ArrayList<>(List.of(
                new BankingProduct(AccountType.SAVINGS.name(), "Account Savings", ProductCategory.ACCOUNTS, false),
                new BankingProduct(AccountType.CHECKING.name(), "Account Corriente", ProductCategory.ACCOUNTS, false),
                new BankingProduct(AccountType.BUSINESS.name(), "Account Companyrial", ProductCategory.ACCOUNTS, true),
                new BankingProduct(AccountType.PERSONAL.name(), "Account Personal", ProductCategory.ACCOUNTS, false)
        ));

        @Override
        public BankingProduct save(BankingProduct bankingProduct) {
            storage.removeIf(existing -> existing.getProductCode().equals(bankingProduct.getProductCode()));
            storage.add(bankingProduct);
            return bankingProduct;
        }

        @Override
        public Optional<BankingProduct> findByProductCode(String productCode) {
            return storage.stream().filter(p -> p.getProductCode().equals(productCode)).findFirst();
        }

        @Override
        public List<BankingProduct> findAll() {
            return List.copyOf(storage);
        }
    }
}

