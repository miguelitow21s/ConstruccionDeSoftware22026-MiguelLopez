package com.bank.application.usecases;

import java.util.List;

import org.springframework.stereotype.Service;

import com.bank.application.ports.CuentaRepositoryPort;
import com.bank.application.ports.TransaccionRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.Transaccion;

@Service
public class ListarTransaccionesUseCase {

    private final TransaccionRepositoryPort transaccionRepository;
    private final CuentaRepositoryPort cuentaRepository;
    private final AuthContextService authContextService;

    public ListarTransaccionesUseCase(TransaccionRepositoryPort transaccionRepository,
                                      CuentaRepositoryPort cuentaRepository,
                                      AuthContextService authContextService) {
        this.transaccionRepository = transaccionRepository;
        this.cuentaRepository = cuentaRepository;
        this.authContextService = authContextService;
    }

    public List<Transaccion> execute() {
        if (authContextService.hasRole("ANALISTA") || authContextService.hasRole("VENTANILLA")) {
            return transaccionRepository.findAll();
        }

        if (authContextService.hasAnyRole("CLIENTE_NATURAL", "CLIENTE_EMPRESA", "EMPLEADO_EMPRESA", "SUPERVISOR_EMPRESA")) {
            String clienteId = authContextService.currentRelatedClientIdOrThrow();
            List<String> cuentasCliente = cuentaRepository.findByClienteId(clienteId).stream()
                    .map(c -> c.getNumeroCuenta().value())
                    .toList();
            if (cuentasCliente.isEmpty()) {
                return List.of();
            }
            return transaccionRepository.findByCuentaOrigenInOrCuentaDestinoIn(cuentasCliente, cuentasCliente);
        }

        return List.of();
    }
}
