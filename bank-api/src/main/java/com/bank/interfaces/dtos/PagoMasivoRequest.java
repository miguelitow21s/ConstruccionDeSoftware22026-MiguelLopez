package com.bank.interfaces.dtos;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record PagoMasivoRequest(
        @NotBlank String cuentaOrigenId,
        @NotEmpty List<@Valid PagoMasivoDetalleRequest> pagos
) {
}
