package com.bank.interfaces.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransferenciaRequest(
        @NotBlank String cuentaOrigenId,
        @NotBlank String cuentaDestinoId,
        @NotNull @DecimalMin("0.01") BigDecimal monto,
        boolean operacionEmpresarial
) {
}
