package com.bank.interfaces.dtos;

import java.math.BigDecimal;

import com.bank.domain.entities.AccountStatus;

public record BalanceResponse(String accountId, BigDecimal balance, AccountStatus statusAccount) {
}
