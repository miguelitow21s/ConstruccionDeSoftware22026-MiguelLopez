package com.bank.interfaces.dtos;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MovimientoRequest(
        @NotBlank String cuentaId,
        @NotBlank @Size(max = 20) String idIdentificacionCliente,
        @NotNull @DecimalMin("0.01") BigDecimal monto
) {
}
