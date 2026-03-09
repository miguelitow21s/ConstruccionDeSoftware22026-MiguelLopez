package com.bank.application.usecases;

import com.bank.application.ports.CuentaRepositoryPort;
import com.bank.domain.services.ServicioCuenta;
import com.bank.domain.valueobjects.Dinero;
import org.springframework.stereotype.Service;

@Service
public class ConsultarSaldoUseCase {

    private final CuentaRepositoryPort cuentaRepository;
    private final ServicioCuenta servicioCuenta;

    public ConsultarSaldoUseCase(CuentaRepositoryPort cuentaRepository, ServicioCuenta servicioCuenta) {
        this.cuentaRepository = cuentaRepository;
        this.servicioCuenta = servicioCuenta;
    }

    public Dinero execute(String cuentaId) {
        return cuentaRepository.findById(cuentaId)
                .map(servicioCuenta::consultarSaldo)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada"));
    }
}
