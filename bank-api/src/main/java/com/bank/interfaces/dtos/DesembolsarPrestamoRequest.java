package com.bank.interfaces.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record DesembolsarPrestamoRequest(
        @NotBlank @Pattern(regexp = "^[0-9]{8,20}$") String numeroCuentaDestino
) {
}
