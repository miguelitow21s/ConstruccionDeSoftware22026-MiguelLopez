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

import com.bank.application.ports.BitacoraEntry;
import com.bank.application.ports.BitacoraRepositoryPort;
import com.bank.application.ports.CuentaRepositoryPort;
import com.bank.application.ports.TransaccionRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.Cuenta;
import com.bank.domain.entities.EstadoCuenta;
import com.bank.domain.entities.EstadoTransaccion;
import com.bank.domain.entities.TipoCuenta;
import com.bank.domain.entities.TipoTransaccion;
import com.bank.domain.entities.Transaccion;
import com.bank.domain.services.ServicioTransferencia;
import com.bank.domain.valueobjects.Dinero;
import com.bank.domain.valueobjects.NumeroCuenta;

class AprobarTransferenciaUseCaseTest {

    @Test
    void debeFallarAprobacionSiNoEsSupervisorEmpresa() {
        FakeTransaccionRepository transaccionRepo = new FakeTransaccionRepository();
        transaccionRepo.storage.add(transaccionEnEspera("t-1"));

        AprobarTransferenciaUseCase useCase = new AprobarTransferenciaUseCase(
                transaccionRepo,
                new FakeCuentaRepository(),
                new ServicioTransferencia(),
                new FakeBitacoraRepository(),
                new AuthContextService("")
        );

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("analista", "123456", "ROLE_ANALISTA")
        );

        SecurityException thrown = assertThrows(SecurityException.class, () -> useCase.aprobar("t-1"));
        assertEquals("Solo Supervisor de Empresa puede aprobar o rechazar transferencias", thrown.getMessage());
    }

    @Test
    void supervisorDebePoderRechazarTransferenciaEnEspera() {
        FakeTransaccionRepository transaccionRepo = new FakeTransaccionRepository();
        transaccionRepo.storage.add(transaccionEnEspera("t-2"));
        FakeCuentaRepository cuentaRepo = new FakeCuentaRepository();
        cuentaRepo.storage.add(cuenta("c-origen", "10000111", "empresa-1"));
        cuentaRepo.storage.add(cuenta("c-destino", "10000112", "empresa-destino"));

        FakeBitacoraRepository bitacoraRepo = new FakeBitacoraRepository();
        AprobarTransferenciaUseCase useCase = new AprobarTransferenciaUseCase(
                transaccionRepo,
            cuentaRepo,
                new ServicioTransferencia(),
                bitacoraRepo,
            new AuthContextService("supervisor:empresa-1")
        );

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("supervisor", "123456", "ROLE_SUPERVISOR_EMPRESA")
        );

        useCase.rechazar("t-2");

        Transaccion actualizada = transaccionRepo.findById("t-2").orElseThrow();
        assertEquals(EstadoTransaccion.RECHAZADA, actualizada.getEstado());
        assertEquals((Long) (long) Math.abs("supervisor".hashCode()), actualizada.getIdUsuarioAprobador());
        assertEquals(1, bitacoraRepo.storage.size());
        assertEquals("Transferencia_Rechazada", bitacoraRepo.storage.getFirst().tipoOperacion());
    }

    @Test
    void supervisorDebePoderAprobarTransferenciaYRegistrarTrazabilidad() {
        FakeTransaccionRepository transaccionRepo = new FakeTransaccionRepository();
        transaccionRepo.storage.add(transaccionEnEspera("t-4"));
        FakeCuentaRepository cuentaRepo = new FakeCuentaRepository();
        cuentaRepo.storage.add(cuenta("c-origen", "10000111", "empresa-1"));
        cuentaRepo.storage.add(cuenta("c-destino", "10000112", "empresa-destino"));

        FakeBitacoraRepository bitacoraRepo = new FakeBitacoraRepository();
        AprobarTransferenciaUseCase useCase = new AprobarTransferenciaUseCase(
                transaccionRepo,
                cuentaRepo,
                new ServicioTransferencia(),
                bitacoraRepo,
                new AuthContextService("supervisor:empresa-1")
        );

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("supervisor", "123456", "ROLE_SUPERVISOR_EMPRESA")
        );

        useCase.aprobar("t-4");

        Transaccion actualizada = transaccionRepo.findById("t-4").orElseThrow();
        assertEquals(EstadoTransaccion.EJECUTADA, actualizada.getEstado());
        assertEquals((Long) (long) Math.abs("supervisor".hashCode()), actualizada.getIdUsuarioAprobador());
        assertEquals(1, bitacoraRepo.storage.size());
        assertEquals("Transferencia_Aprobada", bitacoraRepo.storage.getFirst().tipoOperacion());
    }

    @Test
    void supervisorNoPuedeAprobarTransferenciaDeOtraEmpresa() {
        FakeTransaccionRepository transaccionRepo = new FakeTransaccionRepository();
        transaccionRepo.storage.add(transaccionEnEspera("t-3"));
        FakeCuentaRepository cuentaRepo = new FakeCuentaRepository();
        cuentaRepo.storage.add(cuenta("c-origen", "10000111", "empresa-ajena"));
        cuentaRepo.storage.add(cuenta("c-destino", "10000112", "empresa-destino"));

        AprobarTransferenciaUseCase useCase = new AprobarTransferenciaUseCase(
                transaccionRepo,
                cuentaRepo,
                new ServicioTransferencia(),
                new FakeBitacoraRepository(),
                new AuthContextService("supervisor:empresa-1")
        );

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("supervisor", "123456", "ROLE_SUPERVISOR_EMPRESA")
        );

        SecurityException thrown = assertThrows(SecurityException.class, () -> useCase.aprobar("t-3"));
        assertEquals("No autorizado para aprobar o rechazar operaciones de otra empresa", thrown.getMessage());
    }

    private Transaccion transaccionEnEspera(String id) {
        return new Transaccion(
                id,
                TipoTransaccion.TRANSFERENCIA,
                Dinero.positivo(BigDecimal.valueOf(500)),
                LocalDateTime.now(),
                "10000111",
                "10000112",
                EstadoTransaccion.EN_ESPERA_APROBACION
        );
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

    private Cuenta cuenta(String id, String numero, String clienteId) {
        return new Cuenta(
                id,
                new NumeroCuenta(numero),
                new Dinero(BigDecimal.valueOf(1000)),
                TipoCuenta.AHORROS,
                clienteId,
                EstadoCuenta.ACTIVA
        );
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

        @Override
        public List<BitacoraEntry> findByIdProductoAfectadoIn(List<String> idsProductoAfectado) {
            return storage.stream().filter(e -> idsProductoAfectado.contains(e.idProductoAfectado())).toList();
        }
    }
}

