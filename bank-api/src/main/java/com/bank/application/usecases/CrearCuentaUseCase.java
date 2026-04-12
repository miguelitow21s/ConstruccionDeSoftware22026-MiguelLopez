package com.bank.application.usecases;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.bank.application.ports.ClienteRepositoryPort;
import com.bank.application.ports.CuentaRepositoryPort;
import com.bank.application.ports.ProductoBancarioRepositoryPort;
import com.bank.application.ports.UsuarioSistemaRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.CategoriaProducto;
import com.bank.domain.entities.EstadoUsuario;
import com.bank.domain.entities.Cuenta;
import com.bank.domain.entities.TipoCuenta;
import com.bank.domain.valueobjects.Dinero;
import com.bank.domain.valueobjects.NumeroCuenta;

@Service
public class CrearCuentaUseCase {

    private final CuentaRepositoryPort cuentaRepository;
    private final ClienteRepositoryPort clienteRepository;
    private final UsuarioSistemaRepositoryPort usuarioSistemaRepository;
    private final ProductoBancarioRepositoryPort productoBancarioRepository;
    private final AuthContextService authContextService;

    public CrearCuentaUseCase(CuentaRepositoryPort cuentaRepository,
                              ClienteRepositoryPort clienteRepository,
                              UsuarioSistemaRepositoryPort usuarioSistemaRepository,
                              ProductoBancarioRepositoryPort productoBancarioRepository,
                              AuthContextService authContextService) {
        this.cuentaRepository = cuentaRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioSistemaRepository = usuarioSistemaRepository;
        this.productoBancarioRepository = productoBancarioRepository;
        this.authContextService = authContextService;
    }

    public Cuenta execute(String numeroCuenta, BigDecimal saldoInicial, TipoCuenta tipoCuenta, String clienteId) {
        if (!authContextService.hasAnyRole("ANALISTA", "VENTANILLA", "COMERCIAL")) {
            throw new SecurityException("No autorizado para abrir cuentas");
        }

        if (authContextService.hasRole("COMERCIAL")) {
            String clienteRelacionado = authContextService.currentRelatedClientIdOrThrow();
            if (!clienteRelacionado.equals(clienteId)) {
                throw new SecurityException("No autorizado para abrir cuentas para clientes fuera de su gestion");
            }
        }

        if (saldoInicial.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El saldo inicial no puede ser negativo");
        }
        if (tipoCuenta == null) {
            throw new IllegalArgumentException("Tipo de cuenta obligatorio");
        }

        var cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));

        validarClienteActivo(cliente.getIdIdentificacion());
        validarTipoCuentaEnCatalogo(tipoCuenta);

        cuentaRepository.findByNumeroCuenta(numeroCuenta).ifPresent(existing -> {
            throw new IllegalArgumentException("El numero de cuenta ya existe");
        });

        Cuenta cuenta = new Cuenta(
                new NumeroCuenta(numeroCuenta),
                new Dinero(saldoInicial),
                tipoCuenta,
            clienteId,
            cliente.getIdIdentificacion(),
            "COP",
            LocalDate.now()
        );
        return cuentaRepository.save(cuenta);
    }

    private void validarClienteActivo(String idIdentificacionCliente) {
        var usuarioSistema = usuarioSistemaRepository.findByIdIdentificacion(idIdentificacionCliente)
                .orElseThrow(() -> new IllegalStateException("No existe usuario del sistema asociado al cliente"));

        if (usuarioSistema.getEstadoUsuario() != EstadoUsuario.ACTIVO) {
            throw new IllegalStateException("No se puede abrir cuenta para un cliente inactivo o bloqueado");
        }
    }

    private void validarTipoCuentaEnCatalogo(TipoCuenta tipoCuenta) {
        var productoCuenta = productoBancarioRepository.findByCodigoProducto(tipoCuenta.name())
                .orElseThrow(() -> new IllegalArgumentException("El tipo de cuenta no existe en el catalogo bancario"));

        if (productoCuenta.getCategoria() != CategoriaProducto.CUENTAS) {
            throw new IllegalArgumentException("El tipo de cuenta no corresponde a la categoria de cuentas");
        }
    }
}
