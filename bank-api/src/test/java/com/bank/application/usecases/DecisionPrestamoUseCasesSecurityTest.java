package com.bank.application.usecases;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.bank.application.ports.BitacoraEntry;
import com.bank.application.ports.BitacoraRepositoryPort;
import com.bank.application.ports.PrestamoRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.EstadoPrestamo;
import com.bank.domain.entities.Prestamo;
import com.bank.domain.entities.TipoPrestamo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DecisionPrestamoUseCasesSecurityTest {

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void analistaPuedeAprobarYRechazarPrestamos() {
        FakePrestamoRepository repo = new FakePrestamoRepository();
        repo.storage.add(prestamoEnEstudio("p1"));
        repo.storage.add(prestamoEnEstudio("p2"));

        FakeBitacoraRepository bitacoraRepo = new FakeBitacoraRepository();
        AprobarPrestamoUseCase aprobarUseCase = new AprobarPrestamoUseCase(repo, bitacoraRepo, new AuthContextService(""));
        RechazarPrestamoUseCase rechazarUseCase = new RechazarPrestamoUseCase(repo, bitacoraRepo, new AuthContextService(""));

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("analista", "123456", "ROLE_ANALISTA")
        );

        Prestamo aprobado = aprobarUseCase.execute("p1", BigDecimal.valueOf(900));
        Prestamo rechazado = rechazarUseCase.execute("p2");

        assertEquals(EstadoPrestamo.APROBADO, aprobado.getEstado());
        assertEquals(EstadoPrestamo.RECHAZADO, rechazado.getEstado());
        assertEquals(2, bitacoraRepo.storage.size());
    }

    @Test
    void noAnalistaNoPuedeAprobarNiRechazarPrestamos() {
        FakePrestamoRepository repo = new FakePrestamoRepository();
        repo.storage.add(prestamoEnEstudio("p3"));

        AprobarPrestamoUseCase aprobarUseCase = new AprobarPrestamoUseCase(repo, new FakeBitacoraRepository(), new AuthContextService(""));
        RechazarPrestamoUseCase rechazarUseCase = new RechazarPrestamoUseCase(repo, new FakeBitacoraRepository(), new AuthContextService(""));

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("comercial", "123456", "ROLE_COMERCIAL")
        );

        SecurityException aprobarError = assertThrows(SecurityException.class,
                () -> aprobarUseCase.execute("p3", BigDecimal.valueOf(800)));
        SecurityException rechazarError = assertThrows(SecurityException.class,
                () -> rechazarUseCase.execute("p3"));

        assertEquals("No autorizado para aprobar prestamos", aprobarError.getMessage());
        assertEquals("No autorizado para rechazar prestamos", rechazarError.getMessage());
    }

    private Prestamo prestamoEnEstudio(String id) {
        return new Prestamo(
                id,
                TipoPrestamo.CONSUMO,
                "cliente-1",
                BigDecimal.valueOf(1000),
                null,
                BigDecimal.valueOf(1.5),
                12,
                EstadoPrestamo.EN_ESTUDIO,
                null,
                null,
                null
        );
    }

    private static final class FakePrestamoRepository implements PrestamoRepositoryPort {
        private final List<Prestamo> storage = new ArrayList<>();

        @Override
        public Prestamo save(Prestamo prestamo) {
            storage.removeIf(existing -> existing.getId().equals(prestamo.getId()));
            storage.add(prestamo);
            return prestamo;
        }

        @Override
        public Optional<Prestamo> findById(String id) {
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
