package com.bank.interfaces.dtos;

import java.math.BigDecimal;

import com.bank.domain.entities.EstadoCuenta;

public record SaldoResponse(String cuentaId, BigDecimal saldo, EstadoCuenta estadoCuenta) {
}
