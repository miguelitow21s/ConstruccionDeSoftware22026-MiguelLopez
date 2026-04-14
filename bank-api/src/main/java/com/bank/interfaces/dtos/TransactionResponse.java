package com.bank.interfaces.dtos;

import com.bank.domain.entities.TransactionStatus;
import com.bank.domain.entities.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        String id,
        TransactionType typeTransaction,
        BigDecimal amount,
        LocalDateTime date,
        String sourceAccount,
        String destinationAccount,
        TransactionStatus status
) {
}
