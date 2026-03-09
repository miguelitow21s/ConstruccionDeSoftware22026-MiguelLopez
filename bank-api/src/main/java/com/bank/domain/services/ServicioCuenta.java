package com.bank.domain.services;

import com.bank.domain.entities.Cuenta;
import com.bank.domain.valueobjects.Dinero;

public class ServicioCuenta {

    public void depositar(Cuenta cuenta, Dinero monto) {
        cuenta.depositar(monto);
    }

    public void retirar(Cuenta cuenta, Dinero monto) {
        cuenta.retirar(monto);
    }

    public Dinero consultarSaldo(Cuenta cuenta) {
        cuenta.validarCuentaOperativa();
        return cuenta.getSaldo();
    }
}
