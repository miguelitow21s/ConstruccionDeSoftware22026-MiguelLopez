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

import com.bank.application.ports.CuentaRepositoryPort;
import com.bank.application.ports.TransaccionRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.Cuenta;
import com.bank.domain.entities.EstadoCuenta;
import com.bank.domain.entities.EstadoTransaccion;
import com.bank.domain.entities.TipoCuenta;
import com.bank.domain.entities.TipoTransaccion;
import com.bank.domain.entities.Transaccion;
import com.bank.domain.valueobjects.Dinero;
import com.bank.domain.valueobjects.NumeroCuenta;

class ListarTransaccionesUseCaseTest {

    @Test
    void debeFiltrarPorNumeroCuentaDelClienteAutenticado() {
        FakeCuentaRepository cuentaRepo = new FakeCuentaRepository();
        cuentaRepo.storage.add(cuenta("c1", "10000123", "cliente-1"));

        FakeTransaccionRepository transaccionRepo = new FakeTransaccionRepository();
        transaccionRepo.storage.add(transaccion("t1", "10000123", "10000999"));
        transaccionRepo.storage.add(transaccion("t2", "10000888", "10000777"));

        ListarTransaccionesUseCase useCase = new ListarTransaccionesUseCase(
                transaccionRepo,
                cuentaRepo,
                new AuthContextService("cliente_natural:cliente-1")
        );

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("cliente_natural", "123456", "ROLE_CLIENTE_NATURAL")
        );

        List<Transaccion> resultado = useCase.execute();

        assertEquals(1, resultado.size());
        assertEquals("t1", resultado.getFirst().getId());
    }

        @Test
        void comercialSoloVeTransaccionesDeClientesBajoGestion() {
        FakeCuentaRepository cuentaRepo = new FakeCuentaRepository();
        cuentaRepo.storage.add(cuenta("c1", "10000123", "cliente-1"));

        FakeTransaccionRepository transaccionRepo = new FakeTransaccionRepository();
        transaccionRepo.storage.add(transaccion("t1", "10000123", "10000999"));
        transaccionRepo.storage.add(transaccion("t2", "10000888", "10000777"));

        ListarTransaccionesUseCase useCase = new ListarTransaccionesUseCase(
            transaccionRepo,
            cuentaRepo,
            new AuthContextService("comercial:cliente-1")
        );

        SecurityContextHolder.getContext().setAuthentication(
            new TestingAuthenticationToken("comercial", "123456", "ROLE_COMERCIAL")
        );

        List<Transaccion> resultado = useCase.execute();

        assertEquals(1, resultado.size());
        assertEquals("t1", resultado.getFirst().getId());
        }

        @Test
        void debeFallarParaRolNoAutorizado() {
        ListarTransaccionesUseCase useCase = new ListarTransaccionesUseCase(
            new FakeTransaccionRepository(),
            new FakeCuentaRepository(),
            new AuthContextService("")
        );

        SecurityContextHolder.getContext().setAuthentication(
            new TestingAuthenticationToken("invitado", "123456", "ROLE_INVITADO")
        );

        SecurityException thrown = assertThrows(SecurityException.class, useCase::execute);
        assertEquals("No autorizado para consultar transacciones", thrown.getMessage());
        }

    private Cuenta cuenta(String id, String numeroCuenta, String clienteId) {
        return new Cuenta(id, new NumeroCuenta(numeroCuenta), new Dinero(BigDecimal.valueOf(1000)), TipoCuenta.AHORROS, clienteId, EstadoCuenta.ACTIVA);
    }

    private Transaccion transaccion(String id, String cuentaOrigen, String cuentaDestino) {
        return new Transaccion(
                id,
                TipoTransaccion.TRANSFERENCIA,
                Dinero.positivo(BigDecimal.valueOf(100)),
                LocalDateTime.now(),
                cuentaOrigen,
                cuentaDestino,
                EstadoTransaccion.EJECUTADA
        );
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

