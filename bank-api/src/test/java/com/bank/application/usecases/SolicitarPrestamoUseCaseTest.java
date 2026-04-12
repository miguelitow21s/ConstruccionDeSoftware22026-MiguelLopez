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

import com.bank.application.ports.BitacoraEntry;
import com.bank.application.ports.BitacoraRepositoryPort;
import com.bank.application.ports.ClienteRepositoryPort;
import com.bank.application.ports.PrestamoRepositoryPort;
import com.bank.application.ports.UsuarioSistemaRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.Cliente;
import com.bank.domain.entities.EstadoUsuario;
import com.bank.domain.entities.Prestamo;
import com.bank.domain.entities.RolSistema;
import com.bank.domain.entities.TipoPrestamo;
import com.bank.domain.entities.UsuarioSistema;
import com.bank.domain.valueobjects.Email;

class SolicitarPrestamoUseCaseTest {

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void clienteNaturalNoPuedeSolicitarPrestamoParaOtroCliente() {
        FakeClienteRepository clienteRepo = new FakeClienteRepository();
        clienteRepo.storage.add(new Cliente("id-1", "10101010", "Cliente Uno", new Email("uno@bank.com"), "3001111111"));

        SolicitarPrestamoUseCase useCase = new SolicitarPrestamoUseCase(
                new FakePrestamoRepository(),
                clienteRepo,
            usuarioRepoConActivo("id-1", "10101010"),
                new FakeBitacoraRepository(),
                new AuthContextService("cliente_natural:id-1")
        );

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("cliente_natural", "123456", "ROLE_CLIENTE_NATURAL")
        );

        SecurityException thrown = assertThrows(
                SecurityException.class,
                () -> useCase.execute(TipoPrestamo.CONSUMO, "id-otro", BigDecimal.valueOf(1000), BigDecimal.valueOf(1.2), 12)
        );
        assertEquals("No autorizado para solicitar prestamos para otro cliente", thrown.getMessage());
    }

    @Test
    void comercialPuedeSolicitarPrestamoParaClienteExistente() {
        FakeClienteRepository clienteRepo = new FakeClienteRepository();
        clienteRepo.storage.add(new Cliente("id-1", "10101010", "Cliente Uno", new Email("uno@bank.com"), "3001111111"));

        FakePrestamoRepository prestamoRepo = new FakePrestamoRepository();
        SolicitarPrestamoUseCase useCase = new SolicitarPrestamoUseCase(
                prestamoRepo,
                clienteRepo,
                usuarioRepoConActivo("id-1", "10101010"),
                new FakeBitacoraRepository(),
            new AuthContextService("comercial:id-1")
        );

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("comercial", "123456", "ROLE_COMERCIAL")
        );

        Prestamo prestamo = useCase.execute(TipoPrestamo.CONSUMO, "id-1", BigDecimal.valueOf(2000), BigDecimal.valueOf(1.5), 18);

        assertEquals("id-1", prestamo.getClienteSolicitanteId());
        assertEquals(1, prestamoRepo.storage.size());
    }

        @Test
        void comercialNoPuedeSolicitarPrestamoParaClienteFueraDeSuGestion() {
        FakeClienteRepository clienteRepo = new FakeClienteRepository();
        clienteRepo.storage.add(new Cliente("id-1", "10101010", "Cliente Uno", new Email("uno@bank.com"), "3001111111"));
        clienteRepo.storage.add(new Cliente("id-2", "20202020", "Cliente Dos", new Email("dos@bank.com"), "3002222222"));

        SolicitarPrestamoUseCase useCase = new SolicitarPrestamoUseCase(
            new FakePrestamoRepository(),
            clienteRepo,
            usuarioRepoConActivo("id-1", "10101010"),
            new FakeBitacoraRepository(),
            new AuthContextService("comercial:id-1")
        );

        SecurityContextHolder.getContext().setAuthentication(
            new TestingAuthenticationToken("comercial", "123456", "ROLE_COMERCIAL")
        );

        SecurityException thrown = assertThrows(
            SecurityException.class,
            () -> useCase.execute(TipoPrestamo.CONSUMO, "id-2", BigDecimal.valueOf(1200), BigDecimal.valueOf(1.3), 12)
        );
        assertEquals("No autorizado para solicitar prestamos para otro cliente", thrown.getMessage());
        }

    @Test
    void empleadoEmpresaNoPuedeSolicitarPrestamos() {
        FakeClienteRepository clienteRepo = new FakeClienteRepository();
        clienteRepo.storage.add(new Cliente("id-empresa", "30303030", "Empresa Uno", new Email("empresa@bank.com"), "3003333333"));

        SolicitarPrestamoUseCase useCase = new SolicitarPrestamoUseCase(
                new FakePrestamoRepository(),
                clienteRepo,
            usuarioRepoConActivo("id-empresa", "30303030"),
                new FakeBitacoraRepository(),
                new AuthContextService("empleado_empresa:id-empresa")
        );

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("empleado_empresa", "123456", "ROLE_EMPLEADO_EMPRESA")
        );

        SecurityException thrown = assertThrows(
                SecurityException.class,
                () -> useCase.execute(TipoPrestamo.CONSUMO, "id-empresa", BigDecimal.valueOf(1800), BigDecimal.valueOf(1.4), 12)
        );
        assertEquals("No autorizado para solicitar prestamos", thrown.getMessage());
    }

        @Test
        void noDebePermitirSolicitudSiClienteNoEstaActivo() {
        FakeClienteRepository clienteRepo = new FakeClienteRepository();
        clienteRepo.storage.add(new Cliente("id-1", "10101010", "Cliente Uno", new Email("uno@bank.com"), "3001111111"));

        FakeUsuarioSistemaRepository usuarioRepo = new FakeUsuarioSistemaRepository();
        usuarioRepo.storage.add(new UsuarioSistema(
            1L,
            "id-1",
            "Cliente Uno",
            "10101010",
            new Email("uno@bank.com"),
            "3001111111",
            java.time.LocalDate.of(1990, 1, 1),
            "Calle 1",
            RolSistema.CLIENTE_PERSONA_NATURAL,
            EstadoUsuario.INACTIVO
        ));

        SolicitarPrestamoUseCase useCase = new SolicitarPrestamoUseCase(
            new FakePrestamoRepository(),
            clienteRepo,
            usuarioRepo,
            new FakeBitacoraRepository(),
            new AuthContextService("cliente_natural:id-1")
        );

        SecurityContextHolder.getContext().setAuthentication(
            new TestingAuthenticationToken("cliente_natural", "123456", "ROLE_CLIENTE_NATURAL")
        );

        IllegalStateException thrown = assertThrows(
            IllegalStateException.class,
            () -> useCase.execute(TipoPrestamo.CONSUMO, "id-1", BigDecimal.valueOf(1000), BigDecimal.valueOf(1.2), 12)
        );
        assertEquals("El cliente solicitante debe estar activo", thrown.getMessage());
        }

        private static FakeUsuarioSistemaRepository usuarioRepoConActivo(String idRelacionado, String identificacion) {
        FakeUsuarioSistemaRepository repo = new FakeUsuarioSistemaRepository();
        repo.storage.add(new UsuarioSistema(
            Math.abs(idRelacionado.hashCode()) + 1L,
            idRelacionado,
            "Cliente",
            identificacion,
            new Email("cliente-" + identificacion + "@bank.com"),
            "3001234567",
            java.time.LocalDate.of(1990, 1, 1),
            "Calle 123",
            RolSistema.CLIENTE_PERSONA_NATURAL,
            EstadoUsuario.ACTIVO
        ));
        return repo;
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

    private static final class FakeUsuarioSistemaRepository implements UsuarioSistemaRepositoryPort {
        private final List<UsuarioSistema> storage = new ArrayList<>();

        @Override
        public UsuarioSistema save(UsuarioSistema usuarioSistema) {
            storage.removeIf(existing -> existing.getIdUsuario().equals(usuarioSistema.getIdUsuario()));
            storage.add(usuarioSistema);
            return usuarioSistema;
        }

        @Override
        public Optional<UsuarioSistema> findByIdUsuario(Long idUsuario) {
            return storage.stream().filter(u -> u.getIdUsuario().equals(idUsuario)).findFirst();
        }

        @Override
        public Optional<UsuarioSistema> findByIdIdentificacion(String idIdentificacion) {
            return storage.stream().filter(u -> u.getIdIdentificacion().equals(idIdentificacion)).findFirst();
        }
    }
}
