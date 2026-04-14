package com.bank.domain.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Loan {

    private final String id;
    private final LoanType typeLoan;
    private final String applicantClientId;
    private final String applicantClientIdentification;
    private final BigDecimal requestedAmount;
    private BigDecimal approvedAmount;
    private final BigDecimal interestRate;
    private final int termMonths;
    private LoanStatus status;
    private LocalDateTime approvalDate;
    private LocalDateTime disbursementDate;
    private String disbursementDestinationAccount;

    public Loan(LoanType typeLoan,
                    String applicantClientId,
                    String applicantClientIdentification,
                    BigDecimal requestedAmount,
                    BigDecimal interestRate,
                    int termMonths) {
        this(
                UUID.randomUUID().toString(),
                typeLoan,
                applicantClientId,
                applicantClientIdentification,
                requestedAmount,
                null,
                interestRate,
                termMonths,
                LoanStatus.UNDER_REVIEW,
                null,
                null,
                null
        );
    }

    public Loan(LoanType typeLoan,
                    String applicantClientId,
                    BigDecimal requestedAmount,
                    BigDecimal interestRate,
                    int termMonths) {
        this(typeLoan, applicantClientId, applicantClientId, requestedAmount, interestRate, termMonths);
    }

    public Loan(String id,
                    LoanType typeLoan,
                    String applicantClientId,
                BigDecimal requestedAmount,
                BigDecimal approvedAmount,
                BigDecimal interestRate,
                int termMonths,
                LoanStatus status,
                LocalDateTime approvalDate,
                LocalDateTime disbursementDate,
                String disbursementDestinationAccount) {
        this(
            id,
            typeLoan,
            applicantClientId,
            applicantClientId,
            requestedAmount,
            approvedAmount,
            interestRate,
            termMonths,
            status,
            approvalDate,
            disbursementDate,
            disbursementDestinationAccount
        );
        }

        public Loan(String id,
                LoanType typeLoan,
                String applicantClientId,
                    String applicantClientIdentification,
                    BigDecimal requestedAmount,
                    BigDecimal approvedAmount,
                    BigDecimal interestRate,
                    int termMonths,
                    LoanStatus status,
                    LocalDateTime approvalDate,
                    LocalDateTime disbursementDate,
                    String disbursementDestinationAccount) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Invalid loan id");
        }
        if (applicantClientId == null || applicantClientId.isBlank()) {
            throw new IllegalArgumentException("Invalid applicant client");
        }
        if (applicantClientIdentification == null
                || applicantClientIdentification.isBlank()
                || applicantClientIdentification.length() > 20) {
            throw new IllegalArgumentException("Invalid applicant client identification");
        }
        if (requestedAmount == null || requestedAmount.signum() <= 0) {
            throw new IllegalArgumentException("Requested amount must be greater than zero");
        }
        if (interestRate == null || interestRate.signum() <= 0) {
            throw new IllegalArgumentException("Interest rate must be greater than zero");
        }
        if (termMonths <= 0) {
            throw new IllegalArgumentException("Term in months must be greater than zero");
        }
        this.id = id;
        this.typeLoan = typeLoan;
        this.applicantClientId = applicantClientId;
        this.applicantClientIdentification = applicantClientIdentification;
        this.requestedAmount = requestedAmount;
        this.approvedAmount = approvedAmount;
        this.interestRate = interestRate;
        this.termMonths = termMonths;
        this.status = status;
        this.approvalDate = approvalDate;
        this.disbursementDate = disbursementDate;
        this.disbursementDestinationAccount = disbursementDestinationAccount;

        validateStatusConsistency();
    }

    public void approve(BigDecimal approvedAmount) {
        if (status != LoanStatus.UNDER_REVIEW) {
            throw new IllegalStateException("Only loans under review can be approved");
        }
        if (approvedAmount == null || approvedAmount.signum() <= 0) {
            throw new IllegalArgumentException("Approved amount must be greater than zero");
        }
        this.approvedAmount = approvedAmount;
        this.status = LoanStatus.APPROVED;
        this.approvalDate = LocalDateTime.now();
    }

    public void reject() {
        if (status != LoanStatus.UNDER_REVIEW) {
            throw new IllegalStateException("Only loans under review can be rejected");
        }
        this.status = LoanStatus.REJECTED;
    }

    public void disburse(String accountNumberDestination) {
        if (status != LoanStatus.APPROVED) {
            throw new IllegalStateException("Only approved loans can be disbursed");
        }
        if (approvedAmount == null || approvedAmount.signum() <= 0) {
            throw new IllegalStateException("There is no valid approved amount for disbursement");
        }
        if (accountNumberDestination == null || accountNumberDestination.isBlank()) {
            throw new IllegalArgumentException("Disbursement destination account is required");
        }
        this.disbursementDestinationAccount = accountNumberDestination;
        this.status = LoanStatus.DISBURSED;
        this.disbursementDate = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public LoanType getLoanType() {
        return typeLoan;
    }

    public String getApplicantClientId() {
        return applicantClientId;
    }

    public String getApplicantClientIdentification() {
        return applicantClientIdentification;
    }

    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }

    public BigDecimal getApprovedAmount() {
        return approvedAmount;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public int getTermMonths() {
        return termMonths;
    }

    public LoanStatus getStatus() {
        return status;
    }

    public LocalDateTime getApprovalDate() {
        return approvalDate;
    }

    public LocalDateTime getDisbursementDate() {
        return disbursementDate;
    }

    public String getDisbursementDestinationAccount() {
        return disbursementDestinationAccount;
    }

    private void validateStatusConsistency() {
        if ((status == LoanStatus.APPROVED || status == LoanStatus.DISBURSED)
                && (approvedAmount == null || approvedAmount.signum() <= 0)) {
            throw new IllegalArgumentException("Approved amount is required when the loan is approved or disbursed");
        }
        if (status == LoanStatus.DISBURSED) {
            if (disbursementDestinationAccount == null || disbursementDestinationAccount.isBlank()) {
                throw new IllegalArgumentException("Disbursement destination account is required for disbursed loans");
            }
            if (disbursementDate == null) {
                throw new IllegalArgumentException("Disbursement date is required for disbursed loans");
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Loan loan = (Loan) o;
        return Objects.equals(id, loan.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
