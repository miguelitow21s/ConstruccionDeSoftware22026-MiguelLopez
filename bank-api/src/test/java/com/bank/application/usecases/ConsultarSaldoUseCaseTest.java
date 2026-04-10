package com.bank.application.usecases;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.bank.application.ports.ClienteRepositoryPort;
import com.bank.application.ports.CuentaRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.Cliente;
import com.bank.domain.entities.Cuenta;
import com.bank.domain.entities.EstadoCuenta;
import com.bank.domain.entities.TipoCliente;
import com.bank.domain.entities.TipoCuenta;
import com.bank.domain.services.ServicioCuenta;
import com.bank.domain.valueobjects.Dinero;
import com.bank.domain.valueobjects.Email;
import com.bank.domain.valueobjects.NumeroCuenta;

class ConsultarSaldoUseCaseTest {

    @AfterEach
    @SuppressWarnings("unused")
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void clienteNaturalSoloPuedeConsultarSuPropiaCuenta() {
        FakeCuentaRepository cuentaRepo = new FakeCuentaRepository();
        cuentaRepo.storage.add(cuenta("c1", "10000011", "cliente-1", BigDecimal.valueOf(500)));
        FakeClienteRepository clienteRepo = new FakeClienteRepository();
        clienteRepo.storage.add(cliente("cliente-1", "10101010"));

        ConsultarSaldoUseCase useCase = new ConsultarSaldoUseCase(
            cuentaRepo,
            clienteRepo,
                new ServicioCuenta(),
                new AuthContextService("cliente_natural:cliente-1")
        );

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("cliente_natural", "123456", "ROLE_CLIENTE_NATURAL")
        );

        Dinero saldo = useCase.execute("c1", null);
        assertEquals(BigDecimal.valueOf(500).setScale(2), saldo.value());
    }

    @Test
    void clienteNaturalNoPuedeConsultarCuentaDeOtroCliente() {
        FakeCuentaRepository cuentaRepo = new FakeCuentaRepository();
        cuentaRepo.storage.add(cuenta("c2", "10000012", "cliente-2", BigDecimal.valueOf(800)));
        FakeClienteRepository clienteRepo = new FakeClienteRepository();
        clienteRepo.storage.add(cliente("cliente-2", "20202020"));

        ConsultarSaldoUseCase useCase = new ConsultarSaldoUseCase(
            cuentaRepo,
            clienteRepo,
                new ServicioCuenta(),
                new AuthContextService("cliente_natural:cliente-1")
        );

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("cliente_natural", "123456", "ROLE_CLIENTE_NATURAL")
        );

        SecurityException thrown = assertThrows(SecurityException.class, () -> useCase.execute("c2", null));
        assertEquals("No autorizado para consultar esta cuenta", thrown.getMessage());
    }

        @Test
        void ventanillaDebeValidarIdentificacionClienteParaConsultarSaldo() {
        FakeCuentaRepository cuentaRepo = new FakeCuentaRepository();
        cuentaRepo.storage.add(cuenta("c3", "10000013", "cliente-3", BigDecimal.valueOf(1200)));
        FakeClienteRepository clienteRepo = new FakeClienteRepository();
        clienteRepo.storage.add(cliente("cliente-3", "30303030"));

        ConsultarSaldoUseCase useCase = new ConsultarSaldoUseCase(
            cuentaRepo,
            clienteRepo,
            new ServicioCuenta(),
            new AuthContextService("")
        );

        SecurityContextHolder.getContext().setAuthentication(
            new TestingAuthenticationToken("ventanilla", "123456", "ROLE_VENTANILLA")
        );

        Dinero saldo = useCase.execute("c3", "30303030");
        assertEquals(BigDecimal.valueOf(1200).setScale(2), saldo.value());
        }

        @Test
        void ventanillaDebeFallarSiIdentificacionNoCoincide() {
        FakeCuentaRepository cuentaRepo = new FakeCuentaRepository();
        cuentaRepo.storage.add(cuenta("c4", "10000014", "cliente-4", BigDecimal.valueOf(1500)));
        FakeClienteRepository clienteRepo = new FakeClienteRepository();
        clienteRepo.storage.add(cliente("cliente-4", "40404040"));

        ConsultarSaldoUseCase useCase = new ConsultarSaldoUseCase(
            cuentaRepo,
            clienteRepo,
            new ServicioCuenta(),
            new AuthContextService("")
        );

        SecurityContextHolder.getContext().setAuthentication(
            new TestingAuthenticationToken("ventanilla", "123456", "ROLE_VENTANILLA")
        );

        SecurityException thrown = assertThrows(SecurityException.class, () -> useCase.execute("c4", "00000000"));
        assertEquals("Identificacion del cliente no coincide con la cuenta", thrown.getMessage());
        }

    private Cuenta cuenta(String id, String numero, String clienteId, BigDecimal saldo) {
        return new Cuenta(id, new NumeroCuenta(numero), new Dinero(saldo), TipoCuenta.AHORROS, clienteId, EstadoCuenta.ACTIVA);
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
}
