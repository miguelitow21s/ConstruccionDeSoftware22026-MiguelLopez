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

import com.bank.application.ports.ClienteRepositoryPort;
import com.bank.application.ports.CuentaRepositoryPort;
import com.bank.application.ports.TransaccionRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.Cliente;
import com.bank.domain.entities.Cuenta;
import com.bank.domain.entities.EstadoCuenta;
import com.bank.domain.entities.EstadoTransaccion;
import com.bank.domain.entities.TipoCliente;
import com.bank.domain.entities.TipoCuenta;
import com.bank.domain.entities.Transaccion;
import com.bank.domain.services.ServicioCuenta;
import com.bank.domain.valueobjects.Dinero;
import com.bank.domain.valueobjects.Email;
import com.bank.domain.valueobjects.NumeroCuenta;

class DepositarRetirarDineroUseCaseTest {

    @Test
    void depositarDebeValidarIdentificacionCliente() {
        FakeCuentaRepository cuentaRepo = new FakeCuentaRepository();
        cuentaRepo.storage.add(cuenta("c1", "10000201", "cliente-1", BigDecimal.valueOf(500)));
        FakeClienteRepository clienteRepo = new FakeClienteRepository();
        clienteRepo.storage.add(cliente("cliente-1", "10101010"));

        DepositarDineroUseCase useCase = new DepositarDineroUseCase(
                cuentaRepo,
                clienteRepo,
                new FakeTransaccionRepository(),
                new ServicioCuenta(),
                new AuthContextService("")
        );

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("ventanilla", "123456", "ROLE_VENTANILLA")
        );

        SecurityException thrown = assertThrows(
                SecurityException.class,
                () -> useCase.execute("c1", "99999999", BigDecimal.valueOf(100))
        );
        assertEquals("Identificacion del cliente no coincide con la cuenta", thrown.getMessage());
    }

    @Test
    void retirarDebeValidarIdentificacionCliente() {
        FakeCuentaRepository cuentaRepo = new FakeCuentaRepository();
        cuentaRepo.storage.add(cuenta("c2", "10000202", "cliente-2", BigDecimal.valueOf(800)));
        FakeClienteRepository clienteRepo = new FakeClienteRepository();
        clienteRepo.storage.add(cliente("cliente-2", "20202020"));

        RetirarDineroUseCase useCase = new RetirarDineroUseCase(
                cuentaRepo,
                clienteRepo,
                new FakeTransaccionRepository(),
                new ServicioCuenta(),
                new AuthContextService("")
        );

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("ventanilla", "123456", "ROLE_VENTANILLA")
        );

        SecurityException thrown = assertThrows(
                SecurityException.class,
                () -> useCase.execute("c2", "11111111", BigDecimal.valueOf(50))
        );
        assertEquals("Identificacion del cliente no coincide con la cuenta", thrown.getMessage());
    }

    private Cuenta cuenta(String id, String numeroCuenta, String clienteId, BigDecimal saldo) {
        return new Cuenta(id, new NumeroCuenta(numeroCuenta), new Dinero(saldo), TipoCuenta.AHORROS, clienteId, EstadoCuenta.ACTIVA);
    }

    private Cliente cliente(String id, String idIdentificacion) {
        return new Cliente(id, idIdentificacion, "Cliente", new Email(idIdentificacion + "@bank.com"), "3001234567", TipoCliente.CLIENTE_PERSONA_NATURAL, null);
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

    private static final class FakeTransaccionRepository implements TransaccionRepositoryPort {
        private final List<Transaccion> storage = new ArrayList<>();

        @Override
        public Transaccion save(Transaccion transaccion) {
            storage.removeIf(existing -> existing.getId().equals(transaccion.getId()));
            storage.add(transaccion);
            return transaccion;
        }

        @Override
        public Optional<Transaccion> findById(String id) {
            return storage.stream().filter(t -> t.getId().equals(id)).findFirst();
        }

        @Override
        public List<Transaccion> findAll() {
            return List.copyOf(storage);
        }

        @Override
        public List<Transaccion> findByCuentaOrigenInOrCuentaDestinoIn(List<String> cuentasOrigen, List<String> cuentasDestino) {
            return storage.stream()
                    .filter(t -> cuentasOrigen.contains(t.getCuentaOrigen()) || cuentasDestino.contains(t.getCuentaDestino()))
                    .toList();
        }

        @Override
        public List<Transaccion> findByEstadoAndFechaBefore(EstadoTransaccion estado, LocalDateTime fecha) {
            return storage.stream().filter(t -> t.getEstado() == estado && t.getFecha().isBefore(fecha)).toList();
        }
    }
}

