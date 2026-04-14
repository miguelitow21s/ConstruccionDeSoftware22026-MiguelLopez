package com.bank.interfaces.dtos;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record BulkPaymentRequest(
        @NotBlank String sourceAccountId,
        @NotEmpty List<@Valid BulkPaymentDetailRequest> payments
) {
}
