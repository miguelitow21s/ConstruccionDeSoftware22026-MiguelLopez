package com.bank.application.usecases;

import com.bank.application.ports.ClienteRepositoryPort;
import com.bank.application.ports.CuentaRepositoryPort;
import com.bank.domain.entities.Cuenta;
import com.bank.domain.entities.TipoCuenta;
import com.bank.domain.valueobjects.Dinero;
import com.bank.domain.valueobjects.NumeroCuenta;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CrearCuentaUseCase {

    private final CuentaRepositoryPort cuentaRepository;
    private final ClienteRepositoryPort clienteRepository;

    public CrearCuentaUseCase(CuentaRepositoryPort cuentaRepository, ClienteRepositoryPort clienteRepository) {
        this.cuentaRepository = cuentaRepository;
        this.clienteRepository = clienteRepository;
    }

    public Cuenta execute(String numeroCuenta, BigDecimal saldoInicial, TipoCuenta tipoCuenta, String clienteId) {
        if (saldoInicial.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El saldo inicial no puede ser negativo");
        }

        clienteRepository.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));

        cuentaRepository.findByNumeroCuenta(numeroCuenta).ifPresent(existing -> {
            throw new IllegalArgumentException("El numero de cuenta ya existe");
        });

        Cuenta cuenta = new Cuenta(
                new NumeroCuenta(numeroCuenta),
                new Dinero(saldoInicial),
                tipoCuenta,
                clienteId
        );
        return cuentaRepository.save(cuenta);
    }
}
