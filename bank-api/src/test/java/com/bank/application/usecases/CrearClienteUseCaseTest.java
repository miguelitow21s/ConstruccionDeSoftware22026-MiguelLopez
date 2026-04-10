package com.bank.application.usecases;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.bank.application.ports.ClienteRepositoryPort;
import com.bank.domain.entities.Cliente;
import com.bank.domain.entities.TipoCliente;
import com.bank.domain.valueobjects.Email;

class CrearClienteUseCaseTest {

    @Test
    void debeCrearClienteCuandoNoExisteEmailNiIdentificacion() {
        FakeClienteRepository repo = new FakeClienteRepository();
        CrearClienteUseCase useCase = new CrearClienteUseCase(repo);

        Cliente creado = useCase.execute("10101010", "Miguel Lopez", "miguel@bank.com", "3001234567", null, null);

        assertEquals("10101010", creado.getIdIdentificacion());
        assertEquals(1, repo.storage.size());
    }

    @Test
    void debeFallarSiIdentificacionYaExiste() {
        FakeClienteRepository repo = new FakeClienteRepository();
        repo.storage.add(new Cliente("id-1", "10101010", "Cliente Uno", new Email("uno@bank.com"), "3001111111"));

        CrearClienteUseCase useCase = new CrearClienteUseCase(repo);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> useCase.execute("10101010", "Cliente Dos", "dos@bank.com", "3002222222", null, null));
        assertEquals("Ya existe un cliente con esa identificacion", thrown.getMessage());
    }

    @Test
    void debeFallarSiEmailYaExiste() {
        FakeClienteRepository repo = new FakeClienteRepository();
        repo.storage.add(new Cliente("id-1", "10101010", "Cliente Uno", new Email("uno@bank.com"), "3001111111"));

        CrearClienteUseCase useCase = new CrearClienteUseCase(repo);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> useCase.execute("20202020", "Cliente Dos", "uno@bank.com", "3002222222", null, null));
        assertEquals("Ya existe un cliente con ese email", thrown.getMessage());
    }

    @Test
    void debeCrearClienteEmpresaCuandoRepresentanteEsPersonaNatural() {
        FakeClienteRepository repo = new FakeClienteRepository();
        repo.storage.add(new Cliente("rep-1", "11111111", "Representante", new Email("rep@bank.com"), "3001111111", TipoCliente.CLIENTE_PERSONA_NATURAL, null));

        CrearClienteUseCase useCase = new CrearClienteUseCase(repo);

        Cliente empresa = useCase.execute("900999888", "Empresa S.A.S", "empresa@bank.com", "6011234567", "CLIENTE_EMPRESA", "rep-1");

        assertEquals(TipoCliente.CLIENTE_EMPRESA, empresa.getTipoCliente());
        assertEquals("rep-1", empresa.getRepresentanteLegalId());
    }

    @Test
    void debeFallarClienteEmpresaSinRepresentanteLegal() {
        FakeClienteRepository repo = new FakeClienteRepository();
        CrearClienteUseCase useCase = new CrearClienteUseCase(repo);

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute("900999888", "Empresa S.A.S", "empresa@bank.com", "6011234567", "CLIENTE_EMPRESA", null)
        );
        assertEquals("Representante legal obligatorio para cliente empresa", thrown.getMessage());
    }

    @Test
    void debeFallarClienteEmpresaSiRepresentanteNoEsPersonaNatural() {
        FakeClienteRepository repo = new FakeClienteRepository();
        repo.storage.add(new Cliente("rep-2", "900111222", "Otra Empresa", new Email("otra@bank.com"), "6019999999", TipoCliente.CLIENTE_EMPRESA, "rep-x"));

        CrearClienteUseCase useCase = new CrearClienteUseCase(repo);

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute("900999888", "Empresa S.A.S", "empresa@bank.com", "6011234567", "CLIENTE_EMPRESA", "rep-2")
        );
        assertEquals("El representante legal debe ser un cliente persona natural", thrown.getMessage());
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
