package com.bank.application.usecases;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.bank.application.ports.PrestamoRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.EstadoPrestamo;
import com.bank.domain.entities.Prestamo;
import com.bank.domain.entities.TipoPrestamo;

class ListarPrestamosUseCaseTest {

    @Test
    void debeListarTodoCuandoEsAnalista() {
        FakePrestamoRepositoryPort repo = new FakePrestamoRepositoryPort();
        repo.storage.add(prestamo("p1", "cliente-1"));
        repo.storage.add(prestamo("p2", "cliente-2"));

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("analista", "123456", "ROLE_ANALISTA")
        );

        ListarPrestamosUseCase useCase = new ListarPrestamosUseCase(repo, new AuthContextService("cliente_natural:cliente-1"));

        List<Prestamo> resultado = useCase.execute();

        assertEquals(2, resultado.size());
    }

    @Test
    void debeListarSoloPrestamosDelClienteRelacionadoCuandoEsCliente() {
        FakePrestamoRepositoryPort repo = new FakePrestamoRepositoryPort();
        repo.storage.add(prestamo("p1", "cliente-1"));
        repo.storage.add(prestamo("p2", "cliente-2"));

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("cliente_natural", "123456", "ROLE_CLIENTE_NATURAL")
        );

        ListarPrestamosUseCase useCase = new ListarPrestamosUseCase(repo, new AuthContextService("cliente_natural:cliente-1"));

        List<Prestamo> resultado = useCase.execute();

        assertEquals(1, resultado.size());
        assertEquals("cliente-1", resultado.getFirst().getClienteSolicitanteId());
    }

    @Test
    void comercialDebeListarSoloPrestamosDeClientesBajoGestion() {
        FakePrestamoRepositoryPort repo = new FakePrestamoRepositoryPort();
        repo.storage.add(prestamo("p1", "cliente-1"));
        repo.storage.add(prestamo("p2", "cliente-2"));

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("comercial", "123456", "ROLE_COMERCIAL")
        );

        ListarPrestamosUseCase useCase = new ListarPrestamosUseCase(repo, new AuthContextService("comercial:cliente-1"));

        List<Prestamo> resultado = useCase.execute();

        assertEquals(1, resultado.size());
        assertEquals("cliente-1", resultado.getFirst().getClienteSolicitanteId());
    }

    @Test
    void ventanillaNoDebePoderConsultarPrestamos() {
        FakePrestamoRepositoryPort repo = new FakePrestamoRepositoryPort();
        repo.storage.add(prestamo("p1", "cliente-1"));

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("ventanilla", "123456", "ROLE_VENTANILLA")
        );

        ListarPrestamosUseCase useCase = new ListarPrestamosUseCase(repo, new AuthContextService(""));

        SecurityException thrown = assertThrows(SecurityException.class, useCase::execute);
        assertEquals("No autorizado para consultar prestamos", thrown.getMessage());
    }

    private Prestamo prestamo(String id, String clienteId) {
        return new Prestamo(
                id,
                TipoPrestamo.CONSUMO,
                clienteId,
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(800),
                BigDecimal.valueOf(1.5),
                12,
                EstadoPrestamo.EN_ESTUDIO,
                null,
                null,
                null
        );
    }

    private static final class FakePrestamoRepositoryPort implements PrestamoRepositoryPort {

        private final List<Prestamo> storage = new ArrayList<>();

        @Override
        public Prestamo save(Prestamo prestamo) {
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
}

