package com.bank.interfaces.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransferRequest(
        @NotBlank String sourceAccountId,
        @NotBlank String destinationAccountId,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        boolean businessOperation
) {
}
