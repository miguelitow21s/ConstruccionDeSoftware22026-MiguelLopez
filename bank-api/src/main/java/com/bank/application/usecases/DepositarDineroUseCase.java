package com.bank.application.usecases;

import com.bank.application.ports.CuentaRepositoryPort;
import com.bank.application.ports.TransaccionRepositoryPort;
import com.bank.domain.entities.EstadoTransaccion;
import com.bank.domain.entities.TipoTransaccion;
import com.bank.domain.entities.Transaccion;
import com.bank.domain.services.ServicioCuenta;
import com.bank.domain.valueobjects.Dinero;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class DepositarDineroUseCase {

    private final CuentaRepositoryPort cuentaRepository;
    private final TransaccionRepositoryPort transaccionRepository;
    private final ServicioCuenta servicioCuenta;

    public DepositarDineroUseCase(CuentaRepositoryPort cuentaRepository,
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
        servicioCuenta.depositar(cuenta, dinero);
        cuentaRepository.save(cuenta);

        Transaccion transaccion = new Transaccion(
                TipoTransaccion.DEPOSITO,
                dinero,
                cuenta.getNumeroCuenta().value(),
                cuenta.getNumeroCuenta().value(),
                EstadoTransaccion.EJECUTADA
        );
        transaccionRepository.save(transaccion);
    }
}
