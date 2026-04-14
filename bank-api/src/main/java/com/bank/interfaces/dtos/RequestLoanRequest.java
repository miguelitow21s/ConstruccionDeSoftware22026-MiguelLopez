package com.bank.interfaces.dtos;

import com.bank.domain.entities.LoanType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RequestLoanRequest(
        @NotNull LoanType typeLoan,
        @NotBlank String applicantClientId,
        @NotNull @DecimalMin("0.01") BigDecimal requestedAmount,
        @NotNull @DecimalMin("0.0001") BigDecimal interestRate,
        @Min(1) int termMonths
) {
}
