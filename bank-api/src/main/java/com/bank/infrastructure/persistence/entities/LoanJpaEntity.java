package com.bank.infrastructure.persistence.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.bank.domain.entities.LoanStatus;
import com.bank.domain.entities.LoanType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "loans")
public class LoanJpaEntity {

    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanType typeLoan;

    @Column(nullable = false)
    private String applicantClientId;

    @Column(nullable = false, length = 20)
    private String applicantClientIdentification;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal requestedAmount;

    @Column(precision = 19, scale = 2)
    private BigDecimal approvedAmount;

    @Column(nullable = false, precision = 9, scale = 4)
    private BigDecimal interestRate;

    @Column(nullable = false)
    private Integer termMonths;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status;

    private LocalDateTime approvalDate;

    private LocalDateTime disbursementDate;

    @Column(length = 20)
    private String disbursementDestinationAccount;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LoanType getLoanType() {
        return typeLoan;
    }

    public void setLoanType(LoanType typeLoan) {
        this.typeLoan = typeLoan;
    }

    public String getApplicantClientId() {
        return applicantClientId;
    }

    public void setClientApplicantId(String applicantClientId) {
        this.applicantClientId = applicantClientId;
    }

    public String getApplicantClientIdentification() {
        return applicantClientIdentification;
    }

    public void setClientApplicantIdentification(String applicantClientIdentification) {
        this.applicantClientIdentification = applicantClientIdentification;
    }

    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }

    public void setRequestedAmount(BigDecimal requestedAmount) {
        this.requestedAmount = requestedAmount;
    }

    public BigDecimal getApprovedAmount() {
        return approvedAmount;
    }

    public void setApprovedAmount(BigDecimal approvedAmount) {
        this.approvedAmount = approvedAmount;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public Integer getTermMonths() {
        return termMonths;
    }

    public void setTermMeses(Integer termMonths) {
        this.termMonths = termMonths;
    }

    public LoanStatus getStatus() {
        return status;
    }

    public void setStatus(LoanStatus status) {
        this.status = status;
    }

    public LocalDateTime getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(LocalDateTime approvalDate) {
        this.approvalDate = approvalDate;
    }

    public LocalDateTime getDisbursementDate() {
        return disbursementDate;
    }

    public void setDisbursementDate(LocalDateTime disbursementDate) {
        this.disbursementDate = disbursementDate;
    }

    public String getDisbursementDestinationAccount() {
        return disbursementDestinationAccount;
    }

    public void setAccountDestinationDisbursement(String disbursementDestinationAccount) {
        this.disbursementDestinationAccount = disbursementDestinationAccount;
    }
}
