package com.bank.application.usecases;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.bank.application.ports.BitacoraEntry;
import com.bank.application.ports.BitacoraRepositoryPort;
import com.bank.application.services.AuthContextService;

class ListarBitacoraUseCaseTest {

    @AfterEach
    @SuppressWarnings("unused")
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

        ListarBitacoraUseCase useCase = new ListarBitacoraUseCase(repo, new AuthContextService(""));

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

        ListarBitacoraUseCase useCase = new ListarBitacoraUseCase(repo, new AuthContextService(""));

        List<BitacoraEntry> resultado = useCase.execute(null);

        assertEquals(2, resultado.size());
    }

    @Test
    void noAnalistaDebeVerSoloSuBitacora() {
        FakeBitacoraRepositoryPort repo = new FakeBitacoraRepositoryPort();
        repo.storage.add(entry("cliente_natural", "b1"));
        repo.storage.add(entry("otro", "b2"));

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("cliente_natural", "123456", "ROLE_CLIENTE_NATURAL")
        );

        ListarBitacoraUseCase useCase = new ListarBitacoraUseCase(repo, new AuthContextService(""));

        List<BitacoraEntry> resultado = useCase.execute("otro");

        assertEquals(1, resultado.size());
        assertEquals("cliente_natural", resultado.getFirst().idUsuario());
    }

    private BitacoraEntry entry(String idUsuario, String idBitacora) {
        return new BitacoraEntry(
                idBitacora,
                "Transferencia_Creada",
                LocalDateTime.now(),
                idUsuario,
                "ROLE_CLIENTE_NATURAL",
                "producto-1",
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
    }
}
