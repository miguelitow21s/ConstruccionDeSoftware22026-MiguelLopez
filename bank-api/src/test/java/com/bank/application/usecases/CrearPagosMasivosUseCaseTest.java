package com.bank.application.usecases;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
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
import com.bank.domain.entities.Transaccion;
import com.bank.domain.services.ServicioTransferencia;
import com.bank.domain.valueobjects.Dinero;
import com.bank.domain.valueobjects.NumeroCuenta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CrearPagosMasivosUseCaseTest {

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void empleadoEmpresaPuedeCrearPagosMasivosYAplicarUmbral() {
        FakeCuentaRepository cuentaRepo = new FakeCuentaRepository();
        cuentaRepo.storage.add(cuenta("origen", "20000001", "empresa-1", BigDecimal.valueOf(10000)));
        cuentaRepo.storage.add(cuenta("destino-1", "20000002", "empleado-1", BigDecimal.valueOf(0)));
        cuentaRepo.storage.add(cuenta("destino-2", "20000003", "empleado-2", BigDecimal.valueOf(0)));

        FakeTransaccionRepository transaccionRepo = new FakeTransaccionRepository();
        TransferirDineroUseCase transferirDineroUseCase = new TransferirDineroUseCase(
                cuentaRepo,
                transaccionRepo,
                new ServicioTransferencia(),
                new FakeBitacoraRepository(),
                new AuthContextService("empleado_empresa:empresa-1"),
                BigDecimal.valueOf(2000)
        );

        CrearPagosMasivosUseCase useCase = new CrearPagosMasivosUseCase(
                transferirDineroUseCase,
                new AuthContextService("empleado_empresa:empresa-1")
        );

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("empleado_empresa", "123456", "ROLE_EMPLEADO_EMPRESA")
        );

        List<Transaccion> transacciones = useCase.execute(
                "origen",
                List.of(
                        new CrearPagosMasivosUseCase.PagoMasivoItem("destino-1", BigDecimal.valueOf(2500)),
                        new CrearPagosMasivosUseCase.PagoMasivoItem("destino-2", BigDecimal.valueOf(1500))
                )
        );

        assertEquals(2, transacciones.size());
        assertEquals(EstadoTransaccion.EN_ESPERA_APROBACION, transacciones.getFirst().getEstado());
        assertEquals(EstadoTransaccion.EJECUTADA, transacciones.get(1).getEstado());
    }

    @Test
    void debeFallarSiRolNoEsEmpleadoEmpresa() {
        TransferirDineroUseCase transferirDineroUseCase = new TransferirDineroUseCase(
                new FakeCuentaRepository(),
                new FakeTransaccionRepository(),
                new ServicioTransferencia(),
                new FakeBitacoraRepository(),
                new AuthContextService("supervisor_empresa:empresa-1"),
                BigDecimal.valueOf(2000)
        );

        CrearPagosMasivosUseCase useCase = new CrearPagosMasivosUseCase(
                transferirDineroUseCase,
                new AuthContextService("supervisor_empresa:empresa-1")
        );

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("supervisor_empresa", "123456", "ROLE_SUPERVISOR_EMPRESA")
        );

        SecurityException thrown = assertThrows(
                SecurityException.class,
                () -> useCase.execute("origen", List.of(new CrearPagosMasivosUseCase.PagoMasivoItem("destino", BigDecimal.ONE)))
        );
        assertEquals("No autorizado para crear pagos masivos", thrown.getMessage());
    }

    private Cuenta cuenta(String id, String numero, String clienteId, BigDecimal saldo) {
        return new Cuenta(id, new NumeroCuenta(numero), new Dinero(saldo), TipoCuenta.AHORROS, clienteId, EstadoCuenta.ACTIVA);
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
        @Override
        public void save(BitacoraEntry entry) {
        }

        @Override
        public List<BitacoraEntry> findAll() {
            return List.of();
        }

        @Override
        public List<BitacoraEntry> findByIdUsuario(String idUsuario) {
            return List.of();
        }

        @Override
        public List<BitacoraEntry> findByIdProductoAfectadoIn(List<String> idsProductoAfectado) {
            return List.of(); // Placeholder implementation
        }
    }
}
