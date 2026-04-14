package com.bank.interfaces.dtos;

import com.bank.domain.entities.LoanStatus;
import com.bank.domain.entities.LoanType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LoanResponse(
        String id,
        LoanType typeLoan,
        String applicantClientId,
        BigDecimal requestedAmount,
        BigDecimal approvedAmount,
        BigDecimal interestRate,
        int termMonths,
        LoanStatus status,
        LocalDateTime approvalDate,
        LocalDateTime disbursementDate,
        String disbursementDestinationAccount
) {
}
