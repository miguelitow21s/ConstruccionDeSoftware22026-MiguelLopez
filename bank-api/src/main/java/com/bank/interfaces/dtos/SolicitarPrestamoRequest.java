package com.bank.interfaces.dtos;

import com.bank.domain.entities.TipoPrestamo;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SolicitarPrestamoRequest(
        @NotNull TipoPrestamo tipoPrestamo,
        @NotBlank String clienteSolicitanteId,
        @NotNull @DecimalMin("0.01") BigDecimal montoSolicitado,
        @NotNull @DecimalMin("0.0001") BigDecimal tasaInteres,
        @Min(1) int plazoMeses
) {
}
