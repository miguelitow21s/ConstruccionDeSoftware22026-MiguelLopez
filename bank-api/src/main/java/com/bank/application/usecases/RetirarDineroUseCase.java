package com.bank.application.usecases;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.application.ports.CuentaRepositoryPort;
import com.bank.application.ports.TransaccionRepositoryPort;
import com.bank.domain.entities.EstadoTransaccion;
import com.bank.domain.entities.TipoTransaccion;
import com.bank.domain.entities.Transaccion;
import com.bank.domain.services.ServicioCuenta;
import com.bank.domain.valueobjects.Dinero;

@Service
public class RetirarDineroUseCase {

    private final CuentaRepositoryPort cuentaRepository;
    private final TransaccionRepositoryPort transaccionRepository;
    private final ServicioCuenta servicioCuenta;

    public RetirarDineroUseCase(CuentaRepositoryPort cuentaRepository,
                                TransaccionRepositoryPort transaccionRepository,
                                ServicioCuenta servicioCuenta) {
        this.cuentaRepository = cuentaRepository;
        this.transaccionRepository = transaccionRepository;
        this.servicioCuenta = servicioCuenta;
    }

    @Transactional
    public void execute(String cuentaId, BigDecimal monto) {
        var cuenta = cuentaRepository.findById(cuentaId)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada"));

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
