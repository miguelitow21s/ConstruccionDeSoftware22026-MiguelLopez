package com.bank.application.usecases;

import com.bank.application.ports.BitacoraEntry;
import com.bank.application.ports.BitacoraRepositoryPort;
import com.bank.application.ports.CuentaRepositoryPort;
import com.bank.application.ports.PrestamoRepositoryPort;
import com.bank.application.ports.TransaccionRepositoryPort;
import com.bank.application.services.AuthContextService;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ListarBitacoraUseCase {

    private final BitacoraRepositoryPort bitacoraRepository;
    private final CuentaRepositoryPort cuentaRepository;
    private final PrestamoRepositoryPort prestamoRepository;
    private final TransaccionRepositoryPort transaccionRepository;
    private final AuthContextService authContextService;

    public ListarBitacoraUseCase(BitacoraRepositoryPort bitacoraRepository,
                                 CuentaRepositoryPort cuentaRepository,
                                 PrestamoRepositoryPort prestamoRepository,
                                 TransaccionRepositoryPort transaccionRepository,
                                 AuthContextService authContextService) {
        this.bitacoraRepository = bitacoraRepository;
        this.cuentaRepository = cuentaRepository;
        this.prestamoRepository = prestamoRepository;
        this.transaccionRepository = transaccionRepository;
        this.authContextService = authContextService;
    }

    public List<BitacoraEntry> execute(String idUsuario) {
        return execute(idUsuario, null);
    }

    public List<BitacoraEntry> execute(String idUsuario, String idProductoAfectado) {
        if (!authContextService.hasRole("ANALISTA")) {
            if (!authContextService.hasAnyRole("CLIENTE_NATURAL", "CLIENTE_EMPRESA", "EMPLEADO_EMPRESA", "SUPERVISOR_EMPRESA")) {
                throw new SecurityException("No autorizado para consultar bitacora");
            }

            Set<String> productosPropios = idsProductoPropiosDelCliente();
            if (idProductoAfectado != null && !idProductoAfectado.isBlank() && !productosPropios.contains(idProductoAfectado)) {
                throw new SecurityException("No autorizado para consultar bitacora de productos ajenos");
            }

            List<String> filtroProductos = (idProductoAfectado == null || idProductoAfectado.isBlank())
                    ? List.copyOf(productosPropios)
                    : List.of(idProductoAfectado);
            return bitacoraRepository.findByIdProductoAfectadoIn(filtroProductos);
        }

        if (idProductoAfectado != null && !idProductoAfectado.isBlank()) {
            return bitacoraRepository.findByIdProductoAfectadoIn(List.of(idProductoAfectado));
        }
        if (idUsuario == null || idUsuario.isBlank()) {
            return bitacoraRepository.findAll();
        }
        return bitacoraRepository.findByIdUsuario(idUsuario);
    }

    private Set<String> idsProductoPropiosDelCliente() {
        String clienteId = authContextService.currentRelatedClientIdOrThrow();

        Set<String> idsProducto = new HashSet<>();

        var cuentasCliente = cuentaRepository.findByClienteId(clienteId);
        cuentasCliente.forEach(cuenta -> idsProducto.add(cuenta.getId()));

        prestamoRepository.findByClienteSolicitanteId(clienteId)
                .forEach(prestamo -> idsProducto.add(prestamo.getId()));

        List<String> numerosCuentaCliente = cuentasCliente.stream()
                .map(cuenta -> cuenta.getNumeroCuenta().value())
                .toList();

        if (!numerosCuentaCliente.isEmpty()) {
            transaccionRepository.findByCuentaOrigenInOrCuentaDestinoIn(numerosCuentaCliente, numerosCuentaCliente)
                    .forEach(transaccion -> idsProducto.add(transaccion.getId()));
        }

        return idsProducto;
    }
}
