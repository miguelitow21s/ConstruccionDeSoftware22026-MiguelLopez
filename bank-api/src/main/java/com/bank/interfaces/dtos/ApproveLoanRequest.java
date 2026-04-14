package com.bank.interfaces.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ApproveLoanRequest(
        @NotNull @DecimalMin("0.01") BigDecimal approvedAmount
) {
}
