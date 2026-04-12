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

import com.bank.application.ports.ClienteRepositoryPort;
import com.bank.application.ports.CuentaRepositoryPort;
import com.bank.application.ports.ProductoBancarioRepositoryPort;
import com.bank.application.ports.UsuarioSistemaRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.Cliente;
import com.bank.domain.entities.CategoriaProducto;
import com.bank.domain.entities.Cuenta;
import com.bank.domain.entities.EstadoUsuario;
import com.bank.domain.entities.ProductoBancario;
import com.bank.domain.entities.RolSistema;
import com.bank.domain.entities.TipoCliente;
import com.bank.domain.entities.TipoCuenta;
import com.bank.domain.entities.UsuarioSistema;
import com.bank.domain.valueobjects.Email;

class CrearCuentaUseCaseTest {

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void comercialPuedeAbrirCuentaParaClienteBajoGestion() {
        FakeClienteRepository clienteRepo = new FakeClienteRepository();
        clienteRepo.storage.add(new Cliente("id-1", "10101010", "Cliente Uno", new Email("uno@bank.com"), "3001111111", TipoCliente.CLIENTE_PERSONA_NATURAL, null));
        FakeUsuarioSistemaRepository usuarioRepo = new FakeUsuarioSistemaRepository();
        usuarioRepo.storage.add(usuarioActivo("id-1", "10101010"));

        CrearCuentaUseCase useCase = new CrearCuentaUseCase(
                new FakeCuentaRepository(),
                clienteRepo,
            usuarioRepo,
            new FakeProductoBancarioRepository(),
                new AuthContextService("comercial:id-1")
        );

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("comercial", "123456", "ROLE_COMERCIAL")
        );

        Cuenta cuenta = useCase.execute("12345678", BigDecimal.valueOf(1000), TipoCuenta.AHORROS, "id-1");

        assertEquals("id-1", cuenta.getClienteId());
    }

    @Test
    void comercialNoPuedeAbrirCuentaParaClienteFueraDeGestion() {
        FakeClienteRepository clienteRepo = new FakeClienteRepository();
        clienteRepo.storage.add(new Cliente("id-1", "10101010", "Cliente Uno", new Email("uno@bank.com"), "3001111111", TipoCliente.CLIENTE_PERSONA_NATURAL, null));
        clienteRepo.storage.add(new Cliente("id-2", "20202020", "Cliente Dos", new Email("dos@bank.com"), "3002222222", TipoCliente.CLIENTE_PERSONA_NATURAL, null));
        FakeUsuarioSistemaRepository usuarioRepo = new FakeUsuarioSistemaRepository();
        usuarioRepo.storage.add(usuarioActivo("id-1", "10101010"));
        usuarioRepo.storage.add(usuarioActivo("id-2", "20202020"));

        CrearCuentaUseCase useCase = new CrearCuentaUseCase(
                new FakeCuentaRepository(),
                clienteRepo,
            usuarioRepo,
            new FakeProductoBancarioRepository(),
                new AuthContextService("comercial:id-1")
        );

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("comercial", "123456", "ROLE_COMERCIAL")
        );

        SecurityException thrown = assertThrows(
                SecurityException.class,
                () -> useCase.execute("12345679", BigDecimal.valueOf(1000), TipoCuenta.AHORROS, "id-2")
        );
        assertEquals("No autorizado para abrir cuentas para clientes fuera de su gestion", thrown.getMessage());
    }

        @Test
        void noDebeAbrirCuentaSiClienteEstaInactivoOBloqueado() {
        FakeClienteRepository clienteRepo = new FakeClienteRepository();
        clienteRepo.storage.add(new Cliente("id-1", "10101010", "Cliente Uno", new Email("uno@bank.com"), "3001111111", TipoCliente.CLIENTE_PERSONA_NATURAL, null));

        FakeUsuarioSistemaRepository usuarioRepo = new FakeUsuarioSistemaRepository();
        usuarioRepo.storage.add(new UsuarioSistema(
            10L,
            "id-1",
            "Cliente Uno",
            "10101010",
            new Email("uno@bank.com"),
            "3001111111",
            java.time.LocalDate.of(1990, 1, 1),
            "Calle 1",
            RolSistema.CLIENTE_PERSONA_NATURAL,
            EstadoUsuario.BLOQUEADO
        ));

        CrearCuentaUseCase useCase = new CrearCuentaUseCase(
            new FakeCuentaRepository(),
            clienteRepo,
            usuarioRepo,
            new FakeProductoBancarioRepository(),
            new AuthContextService("comercial:id-1")
        );

        SecurityContextHolder.getContext().setAuthentication(
            new TestingAuthenticationToken("comercial", "123456", "ROLE_COMERCIAL")
        );

        IllegalStateException thrown = assertThrows(
            IllegalStateException.class,
            () -> useCase.execute("12345680", BigDecimal.valueOf(1000), TipoCuenta.AHORROS, "id-1")
        );
        assertEquals("No se puede abrir cuenta para un cliente inactivo o bloqueado", thrown.getMessage());
        }

        @Test
        void noDebeAbrirCuentaConTipoNoRegistradoEnCatalogo() {
        FakeClienteRepository clienteRepo = new FakeClienteRepository();
        clienteRepo.storage.add(new Cliente("id-1", "10101010", "Cliente Uno", new Email("uno@bank.com"), "3001111111", TipoCliente.CLIENTE_PERSONA_NATURAL, null));

        FakeProductoBancarioRepository productoRepo = new FakeProductoBancarioRepository();
        productoRepo.storage.removeIf(producto -> producto.getCodigoProducto().equals(TipoCuenta.AHORROS.name()));

        CrearCuentaUseCase useCase = new CrearCuentaUseCase(
            new FakeCuentaRepository(),
            clienteRepo,
            usuarioRepoConActivo("id-1", "10101010"),
            productoRepo,
            new AuthContextService("comercial:id-1")
        );

        SecurityContextHolder.getContext().setAuthentication(
            new TestingAuthenticationToken("comercial", "123456", "ROLE_COMERCIAL")
        );

        IllegalArgumentException thrown = assertThrows(
            IllegalArgumentException.class,
            () -> useCase.execute("12345681", BigDecimal.valueOf(1000), TipoCuenta.AHORROS, "id-1")
        );
        assertEquals("El tipo de cuenta no existe en el catalogo bancario", thrown.getMessage());
        }

        private static FakeUsuarioSistemaRepository usuarioRepoConActivo(String idRelacionado, String identificacion) {
        FakeUsuarioSistemaRepository repo = new FakeUsuarioSistemaRepository();
        repo.storage.add(usuarioActivo(idRelacionado, identificacion));
        return repo;
        }

        private static UsuarioSistema usuarioActivo(String idRelacionado, String identificacion) {
        return new UsuarioSistema(
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

    private static final class FakeProductoBancarioRepository implements ProductoBancarioRepositoryPort {
        private final List<ProductoBancario> storage = new ArrayList<>(List.of(
                new ProductoBancario(TipoCuenta.AHORROS.name(), "Cuenta Ahorros", CategoriaProducto.CUENTAS, false),
                new ProductoBancario(TipoCuenta.CORRIENTE.name(), "Cuenta Corriente", CategoriaProducto.CUENTAS, false),
                new ProductoBancario(TipoCuenta.EMPRESARIAL.name(), "Cuenta Empresarial", CategoriaProducto.CUENTAS, true),
                new ProductoBancario(TipoCuenta.PERSONAL.name(), "Cuenta Personal", CategoriaProducto.CUENTAS, false)
        ));

        @Override
        public ProductoBancario save(ProductoBancario productoBancario) {
            storage.removeIf(existing -> existing.getCodigoProducto().equals(productoBancario.getCodigoProducto()));
            storage.add(productoBancario);
            return productoBancario;
        }

        @Override
        public Optional<ProductoBancario> findByCodigoProducto(String codigoProducto) {
            return storage.stream().filter(p -> p.getCodigoProducto().equals(codigoProducto)).findFirst();
        }

        @Override
        public List<ProductoBancario> findAll() {
            return List.copyOf(storage);
        }
    }
}
