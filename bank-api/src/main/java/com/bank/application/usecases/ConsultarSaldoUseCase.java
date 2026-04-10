package com.bank.application.usecases;

import org.springframework.stereotype.Service;

import com.bank.application.ports.ClienteRepositoryPort;
import com.bank.application.ports.CuentaRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.Cuenta;
import com.bank.domain.services.ServicioCuenta;
import com.bank.domain.valueobjects.Dinero;

@Service
public class ConsultarSaldoUseCase {

    private final CuentaRepositoryPort cuentaRepository;
    private final ClienteRepositoryPort clienteRepository;
    private final ServicioCuenta servicioCuenta;
    private final AuthContextService authContextService;

    public ConsultarSaldoUseCase(CuentaRepositoryPort cuentaRepository,
                                 ClienteRepositoryPort clienteRepository,
                                 ServicioCuenta servicioCuenta,
                                 AuthContextService authContextService) {
        this.cuentaRepository = cuentaRepository;
        this.clienteRepository = clienteRepository;
        this.servicioCuenta = servicioCuenta;
        this.authContextService = authContextService;
    }

    public Dinero execute(String cuentaId, String idIdentificacionCliente) {
        return cuentaRepository.findById(cuentaId)
                .map(cuenta -> {
                    validarAccesoCuenta(cuenta, idIdentificacionCliente);
                    return cuenta;
                })
                .map(servicioCuenta::consultarSaldo)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada"));
    }

    public Cuenta obtenerCuenta(String cuentaId) {
        return cuentaRepository.findById(cuentaId)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada"));
    }

    private void validarAccesoCuenta(Cuenta cuenta, String idIdentificacionCliente) {
        String clienteIdCuenta = cuenta.getClienteId();

        if (authContextService.hasRole("VENTANILLA")) {
            if (idIdentificacionCliente == null || idIdentificacionCliente.isBlank()) {
                throw new IllegalArgumentException("Identificacion del cliente obligatoria para operaciones de ventanilla");
            }
            var cliente = clienteRepository.findById(clienteIdCuenta)
                    .orElseThrow(() -> new IllegalArgumentException("Cliente asociado a la cuenta no encontrado"));
            if (!cliente.getIdIdentificacion().equals(idIdentificacionCliente)) {
                throw new SecurityException("Identificacion del cliente no coincide con la cuenta");
            }
            return;
        }

        if (authContextService.hasAnyRole("CLIENTE_NATURAL", "CLIENTE_EMPRESA", "EMPLEADO_EMPRESA", "SUPERVISOR_EMPRESA")) {
            String clienteRelacionado = authContextService.currentRelatedClientIdOrThrow();
            if (!clienteRelacionado.equals(clienteIdCuenta)) {
                throw new SecurityException("No autorizado para consultar esta cuenta");
            }
        }
    }
}
