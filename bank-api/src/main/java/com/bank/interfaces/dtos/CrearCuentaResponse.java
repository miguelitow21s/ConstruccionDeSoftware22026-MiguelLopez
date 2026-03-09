package com.bank.interfaces.dtos;

import com.bank.domain.entities.EstadoCuenta;
import com.bank.domain.entities.TipoCuenta;

import java.math.BigDecimal;

public record CrearCuentaResponse(
        String id,
        String numeroCuenta,
        BigDecimal saldo,
        TipoCuenta tipoCuenta,
        String clienteId,
        EstadoCuenta estado
) {
}
