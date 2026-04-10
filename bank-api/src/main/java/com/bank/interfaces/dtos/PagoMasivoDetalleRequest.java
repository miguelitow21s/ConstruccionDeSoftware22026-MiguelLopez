package com.bank.interfaces.dtos;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PagoMasivoDetalleRequest(
        @NotBlank String cuentaDestinoId,
        @NotNull @DecimalMin("0.01") BigDecimal monto
) {
}
