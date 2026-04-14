package com.bank.interfaces.dtos;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MovementRequest(
        @NotBlank String accountId,
        @NotBlank @Size(max = 20) String identificationIdClient,
        @NotNull @DecimalMin("0.01") BigDecimal amount
) {
}
