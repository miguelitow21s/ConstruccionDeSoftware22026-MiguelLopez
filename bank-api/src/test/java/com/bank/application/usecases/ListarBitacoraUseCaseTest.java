package com.bank.application.usecases;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.bank.application.ports.BitacoraEntry;
import com.bank.application.ports.BitacoraRepositoryPort;
import com.bank.application.ports.CuentaRepositoryPort;
import com.bank.application.ports.PrestamoRepositoryPort;
import com.bank.application.ports.TransaccionRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.Cuenta;
import com.bank.domain.entities.EstadoTransaccion;
import com.bank.domain.entities.Prestamo;
import com.bank.domain.entities.TipoCuenta;
import com.bank.domain.entities.TipoTransaccion;
import com.bank.domain.entities.Transaccion;
import com.bank.domain.valueobjects.Dinero;
import com.bank.domain.valueobjects.NumeroCuenta;

class ListarBitacoraUseCaseTest {

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void analistaDebePoderFiltrarPorUsuario() {
        FakeBitacoraRepositoryPort repo = new FakeBitacoraRepositoryPort();
        repo.storage.add(entry("u1", "b1"));
        repo.storage.add(entry("u2", "b2"));

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("analista", "123456", "ROLE_ANALISTA")
        );

    ListarBitacoraUseCase useCase = new ListarBitacoraUseCase(
        repo,
        new FakeCuentaRepositoryPort(),
        new FakePrestamoRepositoryPort(),
        new FakeTransaccionRepositoryPort(),
        new AuthContextService("")
    );

        List<BitacoraEntry> resultado = useCase.execute("u1");

        assertEquals(1, resultado.size());
        assertEquals("u1", resultado.getFirst().idUsuario());
    }

    @Test
    void analistaDebePoderConsultarBitacoraCompleta() {
        FakeBitacoraRepositoryPort repo = new FakeBitacoraRepositoryPort();
        repo.storage.add(entry("u1", "b1"));
        repo.storage.add(entry("u2", "b2"));

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("analista", "123456", "ROLE_ANALISTA")
        );

        ListarBitacoraUseCase useCase = new ListarBitacoraUseCase(
            repo,
            new FakeCuentaRepositoryPort(),
            new FakePrestamoRepositoryPort(),
            new FakeTransaccionRepositoryPort(),
            new AuthContextService("")
        );

        List<BitacoraEntry> resultado = useCase.execute(null);

        assertEquals(2, resultado.size());
    }

    @Test
        void clienteDebeVerSoloBitacoraDeSusProductos() {
        FakeBitacoraRepositoryPort repo = new FakeBitacoraRepositoryPort();
        repo.storage.add(entry("c1", "b1", "tx-cliente"));
        repo.storage.add(entry("c2", "b2", "tx-ajena"));

        FakeCuentaRepositoryPort cuentas = new FakeCuentaRepositoryPort();
        cuentas.storage.add(new Cuenta("cuenta-1", new NumeroCuenta("11110000"), new Dinero(java.math.BigDecimal.valueOf(2000)), TipoCuenta.AHORROS, "cliente-1", com.bank.domain.entities.EstadoCuenta.ACTIVA));

        FakeTransaccionRepositoryPort transacciones = new FakeTransaccionRepositoryPort();
        transacciones.storage.add(new Transaccion("tx-cliente", TipoTransaccion.TRANSFERENCIA, new Dinero(java.math.BigDecimal.valueOf(100)), LocalDateTime.now().minusMinutes(1), "11110000", "99990000", EstadoTransaccion.EJECUTADA));

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("cliente_natural", "123456", "ROLE_CLIENTE_NATURAL")
        );

        ListarBitacoraUseCase useCase = new ListarBitacoraUseCase(
            repo,
            cuentas,
            new FakePrestamoRepositoryPort(),
            transacciones,
            new AuthContextService("cliente_natural:cliente-1")
        );

        List<BitacoraEntry> resultado = useCase.execute(null, null);

        assertEquals(1, resultado.size());
        assertEquals("tx-cliente", resultado.getFirst().idProductoAfectado());
        }

        @Test
        void clienteNoDebeConsultarBitacoraDeProductoAjeno() {
        FakeBitacoraRepositoryPort repo = new FakeBitacoraRepositoryPort();
        FakeCuentaRepositoryPort cuentas = new FakeCuentaRepositoryPort();
        cuentas.storage.add(new Cuenta("cuenta-1", new NumeroCuenta("11110000"), new Dinero(java.math.BigDecimal.valueOf(2000)), TipoCuenta.AHORROS, "cliente-1", com.bank.domain.entities.EstadoCuenta.ACTIVA));

        SecurityContextHolder.getContext().setAuthentication(
            new TestingAuthenticationToken("cliente_natural", "123456", "ROLE_CLIENTE_NATURAL")
        );

        ListarBitacoraUseCase useCase = new ListarBitacoraUseCase(
            repo,
            cuentas,
            new FakePrestamoRepositoryPort(),
            new FakeTransaccionRepositoryPort(),
            new AuthContextService("cliente_natural:cliente-1")
        );

        SecurityException thrown = assertThrows(SecurityException.class, () -> {
            var ignored = useCase.execute(null, "producto-ajeno");
            assertEquals(0, ignored.size());
        });
        assertEquals("No autorizado para consultar bitacora de productos ajenos", thrown.getMessage());
        }

        @Test
        void ventanillaNoDebeConsultarBitacora() {
        SecurityContextHolder.getContext().setAuthentication(
            new TestingAuthenticationToken("ventanilla", "123456", "ROLE_VENTANILLA")
        );

        ListarBitacoraUseCase useCase = new ListarBitacoraUseCase(
            new FakeBitacoraRepositoryPort(),
            new FakeCuentaRepositoryPort(),
            new FakePrestamoRepositoryPort(),
            new FakeTransaccionRepositoryPort(),
            new AuthContextService("")
        );

        SecurityException thrown = assertThrows(SecurityException.class, () -> {
            var ignored = useCase.execute(null, null);
            assertEquals(0, ignored.size());
        });
        assertEquals("No autorizado para consultar bitacora", thrown.getMessage());
    }

    private BitacoraEntry entry(String idUsuario, String idBitacora) {
        return entry(idUsuario, idBitacora, "producto-1");
        }

        private BitacoraEntry entry(String idUsuario, String idBitacora, String idProducto) {
        return new BitacoraEntry(
                idBitacora,
                "Transferencia_Creada",
                LocalDateTime.now(),
                idUsuario,
                "ROLE_CLIENTE_NATURAL",
            idProducto,
                Map.of("monto", 100)
        );
    }

    private static final class FakeBitacoraRepositoryPort implements BitacoraRepositoryPort {

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

    private static final class FakeCuentaRepositoryPort implements CuentaRepositoryPort {
        private final List<Cuenta> storage = new ArrayList<>();

        @Override
        public Cuenta save(Cuenta cuenta) {
            storage.removeIf(existing -> existing.getId().equals(cuenta.getId()));
            storage.add(cuenta);
            return cuenta;
        }

        @Override
        public java.util.Optional<Cuenta> findById(String id) {
            return storage.stream().filter(c -> c.getId().equals(id)).findFirst();
        }

        @Override
        public java.util.Optional<Cuenta> findByNumeroCuenta(String numeroCuenta) {
            return storage.stream().filter(c -> c.getNumeroCuenta().value().equals(numeroCuenta)).findFirst();
        }

        @Override
        public List<Cuenta> findByClienteId(String clienteId) {
            return storage.stream().filter(c -> c.getClienteId().equals(clienteId)).toList();
        }
    }

    private static final class FakePrestamoRepositoryPort implements PrestamoRepositoryPort {
        private final List<Prestamo> storage = new ArrayList<>();

        @Override
        public Prestamo save(Prestamo prestamo) {
            storage.removeIf(existing -> existing.getId().equals(prestamo.getId()));
            storage.add(prestamo);
            return prestamo;
        }

        @Override
        public java.util.Optional<Prestamo> findById(String id) {
            return storage.stream().filter(p -> p.getId().equals(id)).findFirst();
        }

        @Override
        public List<Prestamo> findAll() {
            return List.copyOf(storage);
        }

        @Override
        public List<Prestamo> findByClienteSolicitanteId(String clienteSolicitanteId) {
            return storage.stream().filter(p -> p.getClienteSolicitanteId().equals(clienteSolicitanteId)).toList();
        }
    }

    private static final class FakeTransaccionRepositoryPort implements TransaccionRepositoryPort {
        private final List<Transaccion> storage = new ArrayList<>();

        @Override
        public Transaccion save(Transaccion transaccion) {
            storage.removeIf(existing -> existing.getId().equals(transaccion.getId()));
            storage.add(transaccion);
            return transaccion;
        }

        @Override
        public java.util.Optional<Transaccion> findById(String id) {
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
