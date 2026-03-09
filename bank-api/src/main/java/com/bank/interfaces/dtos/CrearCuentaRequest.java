package com.bank.interfaces.dtos;

import com.bank.domain.entities.TipoCuenta;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record CrearCuentaRequest(
        @NotBlank @Pattern(regexp = "^[0-9]{8,20}$") String numeroCuenta,
        @NotNull @DecimalMin("0.00") BigDecimal saldoInicial,
        @NotNull TipoCuenta tipoCuenta,
        @NotBlank String clienteId
) {
}
