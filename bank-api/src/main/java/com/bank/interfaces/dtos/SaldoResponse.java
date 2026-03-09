package com.bank.interfaces.dtos;

import java.math.BigDecimal;

public record SaldoResponse(String cuentaId, BigDecimal saldo) {
}
