package com.bank.application.usecases;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.bank.application.ports.ClienteRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.Cliente;
import com.bank.domain.valueobjects.Email;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConsultarClienteUseCaseTest {

    @AfterEach
    @SuppressWarnings("unused")
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void analistaPuedeConsultarCualquierCliente() {
        FakeClienteRepository repo = new FakeClienteRepository();
        repo.storage.add(new Cliente("c1", "10101010", "Cliente Uno", new Email("uno@bank.com"), "3001111111"));

        ConsultarClienteUseCase useCase = new ConsultarClienteUseCase(repo, new AuthContextService(""));

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("analista", "123456", "ROLE_ANALISTA")
        );

        Cliente cliente = useCase.execute("c1");
        assertEquals("c1", cliente.getId());
    }

    @Test
    void analistaNoPuedeConsultarClienteInexistente() {
        ConsultarClienteUseCase useCase = new ConsultarClienteUseCase(new FakeClienteRepository(), new AuthContextService(""));

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("analista", "123456", "ROLE_ANALISTA")
        );

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> useCase.execute("no-existe"));
        assertEquals("Cliente no encontrado", thrown.getMessage());
    }

    @Test
    void comercialNoPuedeConsultarClienteFueraDeSuGestion() {
        FakeClienteRepository repo = new FakeClienteRepository();
        repo.storage.add(new Cliente("c2", "20202020", "Cliente Dos", new Email("dos@bank.com"), "3002222222"));

        ConsultarClienteUseCase useCase = new ConsultarClienteUseCase(repo, new AuthContextService("comercial:c1"));

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("comercial", "123456", "ROLE_COMERCIAL")
        );

        SecurityException thrown = assertThrows(SecurityException.class, () -> useCase.execute("c2"));
        assertEquals("No autorizado para consultar clientes fuera de su gestion", thrown.getMessage());
    }

    private static final class FakeClienteRepository implements ClienteRepositoryPort {
        private final List<Cliente> storage = new ArrayList<>();

        @Override
        public Cliente save(Cliente cliente) {
            storage.removeIf(existing -> existing.getId().equals(cliente.getId()));
            storage.add(cliente);
            return cliente;
        }

        @Override
        public Optional<Cliente> findById(String id) {
            return storage.stream().filter(c -> c.getId().equals(id)).findFirst();
        }

        @Override
        public Optional<Cliente> findByEmail(String email) {
            return storage.stream().filter(c -> c.getEmail().value().equalsIgnoreCase(email)).findFirst();
        }

        @Override
        public Optional<Cliente> findByIdIdentificacion(String idIdentificacion) {
            return storage.stream().filter(c -> c.getIdIdentificacion().equals(idIdentificacion)).findFirst();
        }
    }
}
