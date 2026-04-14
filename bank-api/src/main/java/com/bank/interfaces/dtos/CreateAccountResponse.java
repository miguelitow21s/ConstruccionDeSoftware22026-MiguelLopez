package com.bank.interfaces.dtos;

import com.bank.domain.entities.AccountStatus;
import com.bank.domain.entities.AccountType;

import java.math.BigDecimal;

public record CreateAccountResponse(
        String id,
        String accountNumber,
        BigDecimal balance,
        AccountType accountType,
        String clientId,
        AccountStatus status
) {
}
