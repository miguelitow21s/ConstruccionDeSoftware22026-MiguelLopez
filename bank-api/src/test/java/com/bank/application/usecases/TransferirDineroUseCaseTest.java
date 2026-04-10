package com.bank.application.usecases;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.bank.application.ports.BitacoraEntry;
import com.bank.application.ports.BitacoraRepositoryPort;
import com.bank.application.ports.CuentaRepositoryPort;
import com.bank.application.ports.TransaccionRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.Cuenta;
import com.bank.domain.entities.EstadoTransaccion;
import com.bank.domain.entities.TipoCuenta;
import com.bank.domain.entities.Transaccion;
import com.bank.domain.services.ServicioTransferencia;
import com.bank.domain.valueobjects.Dinero;
import com.bank.domain.valueobjects.NumeroCuenta;

class TransferirDineroUseCaseTest {

    @AfterEach
    @SuppressWarnings("unused")
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void debeFallarSiRolNoAutorizadoParaCrearTransferencia() {
        FakeCuentaRepository cuentaRepo = new FakeCuentaRepository();
        cuentaRepo.storage.add(cuenta("c1", "10000001", "cliente-1", BigDecimal.valueOf(1000)));
        cuentaRepo.storage.add(cuenta("c2", "10000002", "cliente-2", BigDecimal.valueOf(1000)));

        TransferirDineroUseCase useCase = new TransferirDineroUseCase(
                cuentaRepo,
                new FakeTransaccionRepository(),
                new ServicioTransferencia(),
                new FakeBitacoraRepository(),
                new AuthContextService(""),
                BigDecimal.valueOf(10_000)
        );

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("analista", "123456", "ROLE_ANALISTA")
        );

        SecurityException thrown = assertThrows(
            SecurityException.class,
            () -> useCase.execute("c1", "c2", BigDecimal.valueOf(100), false)
        );
        assertEquals("No autorizado para crear transferencias", thrown.getMessage());
    }

    @Test
    void debePermitirTransferenciaConRolClienteNatural() {
        FakeCuentaRepository cuentaRepo = new FakeCuentaRepository();
        cuentaRepo.storage.add(cuenta("c1", "10000003", "cliente-1", BigDecimal.valueOf(1000)));
        cuentaRepo.storage.add(cuenta("c2", "10000004", "cliente-2", BigDecimal.valueOf(1000)));

        FakeTransaccionRepository transaccionRepo = new FakeTransaccionRepository();
        TransferirDineroUseCase useCase = new TransferirDineroUseCase(
                cuentaRepo,
                transaccionRepo,
                new ServicioTransferencia(),
                new FakeBitacoraRepository(),
                new AuthContextService(""),
                BigDecimal.valueOf(10_000)
        );

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("cliente_natural", "123456", "ROLE_CLIENTE_NATURAL")
        );

        Transaccion transaccion = useCase.execute("c1", "c2", BigDecimal.valueOf(100), false);

        assertEquals(EstadoTransaccion.EJECUTADA, transaccion.getEstado());
        assertEquals(1, transaccionRepo.storage.size());
        assertEquals(BigDecimal.valueOf(900).setScale(2), cuentaRepo.findById("c1").orElseThrow().getSaldo().value());
        assertEquals(BigDecimal.valueOf(1100).setScale(2), cuentaRepo.findById("c2").orElseThrow().getSaldo().value());
    }

    private Cuenta cuenta(String id, String numero, String clienteId, BigDecimal saldo) {
        return new Cuenta(
                id,
                new NumeroCuenta(numero),
                new Dinero(saldo),
                TipoCuenta.AHORROS,
                clienteId,
                com.bank.domain.entities.EstadoCuenta.ACTIVA
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

    private static final class FakeBitacoraRepository implements BitacoraRepositoryPort {
        private final List<BitacoraEntry> storage = new ArrayList<>();

        @Override
        public void save(BitacoraEntry entry) {
            storage.add(entry);
        }

        @Override
        public List<BitacoraEntry> findAll() {
            return List.copyOf(storage);
        }

        @Override
        public List<BitacoraEntry> findByIdUsuario(String idUsuario) {
            return storage.stream().filter(e -> e.idUsuario().equals(idUsuario)).toList();
        }
    }
}
