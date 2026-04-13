package com.bank.application.usecases;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.bank.application.ports.BitacoraEntry;
import com.bank.application.ports.BitacoraRepositoryPort;
import com.bank.application.ports.TransaccionRepositoryPort;
import com.bank.domain.entities.EstadoTransaccion;
import com.bank.domain.entities.TipoTransaccion;
import com.bank.domain.entities.Transaccion;
import com.bank.domain.valueobjects.Dinero;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VencerTransferenciasPendientesUseCaseTest {

    @Test
    void debeVencerTransferenciaPendienteFueraDeVentanaYRegistrarBitacora() {
        FakeTransaccionRepository transaccionRepo = new FakeTransaccionRepository();
        transaccionRepo.storage.add(new Transaccion(
                "tx-v1",
                TipoTransaccion.TRANSFERENCIA,
                Dinero.positivo(BigDecimal.valueOf(300)),
                LocalDateTime.now().minusMinutes(61),
                null,
                "10000111",
                "10000112",
                EstadoTransaccion.EN_ESPERA_APROBACION,
                123L,
                null
        ));

        FakeBitacoraRepository bitacoraRepo = new FakeBitacoraRepository();
        VencerTransferenciasPendientesUseCase useCase = new VencerTransferenciasPendientesUseCase(
                transaccionRepo,
                bitacoraRepo,
                60
        );

        useCase.execute();

        Transaccion actualizada = transaccionRepo.findById("tx-v1").orElseThrow();
        assertEquals(EstadoTransaccion.VENCIDA, actualizada.getEstado());
        assertEquals(1, bitacoraRepo.storage.size());
        assertEquals("Transferencia_Vencida", bitacoraRepo.storage.getFirst().tipoOperacion());
        assertEquals("vencida por falta de aprobacion en el tiempo establecido",
                bitacoraRepo.storage.getFirst().datosDetalle().get("motivo"));
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

        @Override
        public List<BitacoraEntry> findByIdProductoAfectadoIn(List<String> idsProductoAfectado) {
            return storage.stream().filter(e -> idsProductoAfectado.contains(e.idProductoAfectado())).toList();
        }
    }
}
