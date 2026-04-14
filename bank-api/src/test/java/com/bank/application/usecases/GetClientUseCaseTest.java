package com.bank.application.usecases;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.bank.application.ports.ClientRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.Client;
import com.bank.domain.valueobjects.Email;

class GetClientUseCaseTest {

    @Test
    void salesPuedeGetClientBajoSuManagement() {
        FakeClientRepository repo = new FakeClientRepository();
        repo.storage.add(new Client("c1", "10101010", "Client Uno", new Email("uno@bank.com"), "3001111111"));

        GetClientUseCase useCase = new GetClientUseCase(repo, new AuthContextService("sales:c1"));

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("sales", "123456", "ROLE_SALES")
        );

        Client client = useCase.execute("c1");
        assertEquals("c1", client.getId());
    }

    @Test
    void analystPuedeGetCualquierClient() {
        FakeClientRepository repo = new FakeClientRepository();
        repo.storage.add(new Client("c1", "10101010", "Client Uno", new Email("uno@bank.com"), "3001111111"));

        GetClientUseCase useCase = new GetClientUseCase(repo, new AuthContextService(""));

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("analyst", "123456", "ROLE_ANALYST")
        );

        Client client = useCase.execute("c1");
        assertEquals("c1", client.getId());
    }

    @Test
    void analystNoPuedeGetClientInexistente() {
        GetClientUseCase useCase = new GetClientUseCase(new FakeClientRepository(), new AuthContextService(""));

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("analyst", "123456", "ROLE_ANALYST")
        );

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> useCase.execute("no-existe"));
        assertEquals("Client not found", thrown.getMessage());
    }

    @Test
    void salesNoPuedeGetClientFueraDeSuManagement() {
        FakeClientRepository repo = new FakeClientRepository();
        repo.storage.add(new Client("c2", "20202020", "Client Dos", new Email("dos@bank.com"), "3002222222"));

        GetClientUseCase useCase = new GetClientUseCase(repo, new AuthContextService("sales:c1"));

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("sales", "123456", "ROLE_SALES")
        );

        SecurityException thrown = assertThrows(SecurityException.class, () -> useCase.execute("c2"));
        assertEquals("Not authorized to get clients outside the authorized scope", thrown.getMessage());
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

