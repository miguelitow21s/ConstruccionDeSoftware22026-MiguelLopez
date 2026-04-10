package com.bank.application.usecases;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.bank.application.ports.ClienteRepositoryPort;
import com.bank.domain.entities.Cliente;
import com.bank.domain.valueobjects.Email;

class CrearClienteUseCaseTest {

    @Test
    void debeCrearClienteCuandoNoExisteEmailNiIdentificacion() {
        FakeClienteRepository repo = new FakeClienteRepository();
        CrearClienteUseCase useCase = new CrearClienteUseCase(repo);

        Cliente creado = useCase.execute("10101010", "Miguel Lopez", "miguel@bank.com", "3001234567");

        assertEquals("10101010", creado.getIdIdentificacion());
        assertEquals(1, repo.storage.size());
    }

    @Test
    void debeFallarSiIdentificacionYaExiste() {
        FakeClienteRepository repo = new FakeClienteRepository();
        repo.storage.add(new Cliente("id-1", "10101010", "Cliente Uno", new Email("uno@bank.com"), "3001111111"));

        CrearClienteUseCase useCase = new CrearClienteUseCase(repo);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> useCase.execute("10101010", "Cliente Dos", "dos@bank.com", "3002222222"));
        assertEquals("Ya existe un cliente con esa identificacion", thrown.getMessage());
    }

    @Test
    void debeFallarSiEmailYaExiste() {
        FakeClienteRepository repo = new FakeClienteRepository();
        repo.storage.add(new Cliente("id-1", "10101010", "Cliente Uno", new Email("uno@bank.com"), "3001111111"));

        CrearClienteUseCase useCase = new CrearClienteUseCase(repo);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> useCase.execute("20202020", "Cliente Dos", "uno@bank.com", "3002222222"));
        assertEquals("Ya existe un cliente con ese email", thrown.getMessage());
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
