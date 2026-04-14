package com.bank.interfaces.dtos;

import com.bank.domain.entities.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record CreateAccountRequest(
        @NotBlank @Pattern(regexp = "^[0-9]{8,20}$") String accountNumber,
        @NotNull @DecimalMin("0.00") BigDecimal initialBalance,
        @NotNull AccountType accountType,
        @NotBlank String clientId
) {
}
