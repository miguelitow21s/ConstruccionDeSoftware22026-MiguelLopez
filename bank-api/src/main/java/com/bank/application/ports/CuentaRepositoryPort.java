package com.bank.application.ports;

import com.bank.domain.entities.Cuenta;

import java.util.Optional;

public interface CuentaRepositoryPort {

    Cuenta save(Cuenta cuenta);

    Optional<Cuenta> findById(String id);

    Optional<Cuenta> findByNumeroCuenta(String numeroCuenta);
}
