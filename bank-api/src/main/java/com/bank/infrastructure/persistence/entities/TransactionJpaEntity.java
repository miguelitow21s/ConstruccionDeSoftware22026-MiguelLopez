package com.bank.infrastructure.persistence.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.bank.domain.entities.TransactionStatus;
import com.bank.domain.entities.TransactionType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "transactions")
public class TransactionJpaEntity {

    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType typeTransaction;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDateTime date;

    private LocalDateTime approvalDate;

    @Column(nullable = false)
    private String sourceAccount;

    @Column(nullable = false)
    private String destinationAccount;

    @Column(nullable = false)
    private Long creatorUserId;

    private Long approverUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TransactionType getTransactionType() {
        return typeTransaction;
    }

    public void setTransactionType(TransactionType typeTransaction) {
        this.typeTransaction = typeTransaction;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public LocalDateTime getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(LocalDateTime approvalDate) {
        this.approvalDate = approvalDate;
    }

    public String getSourceAccount() {
        return sourceAccount;
    }

    public void setAccountSource(String sourceAccount) {
        this.sourceAccount = sourceAccount;
    }

    public String getDestinationAccount() {
        return destinationAccount;
    }

    public void setAccountDestination(String destinationAccount) {
        this.destinationAccount = destinationAccount;
    }

    public Long getCreatorUserId() {
        return creatorUserId;
    }

    public void setCreatorUserId(Long creatorUserId) {
        this.creatorUserId = creatorUserId;
    }

    public Long getApproverUserId() {
        return approverUserId;
    }

    public void setApproverUserId(Long approverUserId) {
        this.approverUserId = approverUserId;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }
}
