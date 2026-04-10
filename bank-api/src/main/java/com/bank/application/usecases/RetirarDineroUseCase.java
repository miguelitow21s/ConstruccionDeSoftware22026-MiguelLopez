package com.bank.application.usecases;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.application.ports.ClienteRepositoryPort;
import com.bank.application.ports.CuentaRepositoryPort;
import com.bank.application.ports.TransaccionRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.EstadoTransaccion;
import com.bank.domain.entities.TipoTransaccion;
import com.bank.domain.entities.Transaccion;
import com.bank.domain.services.ServicioCuenta;
import com.bank.domain.valueobjects.Dinero;

@Service
public class RetirarDineroUseCase {

    private final CuentaRepositoryPort cuentaRepository;
    private final ClienteRepositoryPort clienteRepository;
    private final TransaccionRepositoryPort transaccionRepository;
    private final ServicioCuenta servicioCuenta;
    private final AuthContextService authContextService;

    public RetirarDineroUseCase(CuentaRepositoryPort cuentaRepository,
                                ClienteRepositoryPort clienteRepository,
                                TransaccionRepositoryPort transaccionRepository,
                                ServicioCuenta servicioCuenta,
                                AuthContextService authContextService) {
        this.cuentaRepository = cuentaRepository;
        this.clienteRepository = clienteRepository;
        this.transaccionRepository = transaccionRepository;
        this.servicioCuenta = servicioCuenta;
        this.authContextService = authContextService;
    }

    @Transactional
    public void execute(String cuentaId, String idIdentificacionCliente, BigDecimal monto) {
        if (!authContextService.hasRole("VENTANILLA")) {
            throw new SecurityException("No autorizado para realizar retiros");
        }

        var cuenta = cuentaRepository.findById(cuentaId)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada"));

        if (idIdentificacionCliente == null || idIdentificacionCliente.isBlank()) {
            throw new IllegalArgumentException("Identificacion del cliente obligatoria para operaciones de ventanilla");
        }

        var cliente = clienteRepository.findById(cuenta.getClienteId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente asociado a la cuenta no encontrado"));

        if (!cliente.getIdIdentificacion().equals(idIdentificacionCliente)) {
            throw new SecurityException("Identificacion del cliente no coincide con la cuenta");
        }

        Dinero dinero = Dinero.positivo(monto);
        servicioCuenta.retirar(cuenta, dinero);
        cuentaRepository.save(cuenta);

        Transaccion transaccion = new Transaccion(
                TipoTransaccion.RETIRO,
                dinero,
                cuenta.getNumeroCuenta().value(),
                cuenta.getNumeroCuenta().value(),
                EstadoTransaccion.EJECUTADA
        );
        transaccionRepository.save(transaccion);
    }
}
