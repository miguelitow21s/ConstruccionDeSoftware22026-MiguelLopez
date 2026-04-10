package com.bank.application.usecases;

import com.bank.application.ports.ClienteRepositoryPort;
import com.bank.application.ports.CuentaRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.Cliente;
import com.bank.domain.entities.Cuenta;
import com.bank.domain.entities.EstadoCuenta;
import com.bank.domain.entities.TipoCliente;
import com.bank.domain.entities.TipoCuenta;
import com.bank.domain.valueobjects.Email;
import com.bank.domain.valueobjects.NumeroCuenta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CrearCuentaUseCaseTest {

    @AfterEach
    @SuppressWarnings("unused")
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void comercialPuedeAbrirCuentaParaClienteBajoGestion() {
        FakeClienteRepository clienteRepo = new FakeClienteRepository();
        clienteRepo.storage.add(new Cliente("id-1", "10101010", "Cliente Uno", new Email("uno@bank.com"), "3001111111", TipoCliente.CLIENTE_PERSONA_NATURAL, null));

        CrearCuentaUseCase useCase = new CrearCuentaUseCase(
                new FakeCuentaRepository(),
                clienteRepo,
                new AuthContextService("comercial:id-1")
        );

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("comercial", "123456", "ROLE_COMERCIAL")
        );

        Cuenta cuenta = useCase.execute("12345678", BigDecimal.valueOf(1000), TipoCuenta.AHORROS, "id-1");

        assertEquals("id-1", cuenta.getClienteId());
    }

    @Test
    void comercialNoPuedeAbrirCuentaParaClienteFueraDeGestion() {
        FakeClienteRepository clienteRepo = new FakeClienteRepository();
        clienteRepo.storage.add(new Cliente("id-1", "10101010", "Cliente Uno", new Email("uno@bank.com"), "3001111111", TipoCliente.CLIENTE_PERSONA_NATURAL, null));
        clienteRepo.storage.add(new Cliente("id-2", "20202020", "Cliente Dos", new Email("dos@bank.com"), "3002222222", TipoCliente.CLIENTE_PERSONA_NATURAL, null));

        CrearCuentaUseCase useCase = new CrearCuentaUseCase(
                new FakeCuentaRepository(),
                clienteRepo,
                new AuthContextService("comercial:id-1")
        );

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("comercial", "123456", "ROLE_COMERCIAL")
        );

        SecurityException thrown = assertThrows(
                SecurityException.class,
                () -> useCase.execute("12345679", BigDecimal.valueOf(1000), TipoCuenta.AHORROS, "id-2")
        );
        assertEquals("No autorizado para abrir cuentas para clientes fuera de su gestion", thrown.getMessage());
    }

    private static final class FakeCuentaRepository implements CuentaRepositoryPort {
        private final List<Cuenta> storage = new ArrayList<>();

        @Override
        public Cuenta save(Cuenta cuenta) {
            storage.removeIf(existing -> existing.getId().equals(cuenta.getId()));
            storage.add(cuenta);
            return cuenta;
        }

        @Override
        public Optional<Cuenta> findById(String id) {
            return storage.stream().filter(c -> c.getId().equals(id)).findFirst();
        }

        @Override
        public Optional<Cuenta> findByNumeroCuenta(String numeroCuenta) {
            return storage.stream().filter(c -> c.getNumeroCuenta().value().equals(numeroCuenta)).findFirst();
        }

        @Override
        public List<Cuenta> findByClienteId(String clienteId) {
            return storage.stream().filter(c -> c.getClienteId().equals(clienteId)).toList();
        }
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
