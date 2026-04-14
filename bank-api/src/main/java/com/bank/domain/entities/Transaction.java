package com.bank.domain.entities;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import com.bank.domain.valueobjects.Money;

public class Transaction {

    private final String id;
    private final TransactionType typeTransaction;
    private final Money amount;
    private final LocalDateTime date;
    private LocalDateTime approvalDate;
    private final String sourceAccount;
    private final String destinationAccount;
    private final Long creatorUserId;
    private Long approverUserId;
    private TransactionStatus status;

    public Transaction(TransactionType typeTransaction, Money amount, String sourceAccount, String destinationAccount, TransactionStatus status) {
        this(UUID.randomUUID().toString(), typeTransaction, amount, LocalDateTime.now(), null, sourceAccount, destinationAccount, status, 0L, null);
    }

    public Transaction(TransactionType typeTransaction,
                       Money amount,
                       String sourceAccount,
                       String destinationAccount,
                       TransactionStatus status,
                       Long creatorUserId) {
        this(UUID.randomUUID().toString(), typeTransaction, amount, LocalDateTime.now(), null, sourceAccount, destinationAccount, status, creatorUserId, null);
    }

    public Transaction(String id, TransactionType typeTransaction, Money amount, LocalDateTime date,
                       String sourceAccount, String destinationAccount, TransactionStatus status) {
        this(id, typeTransaction, amount, date, null, sourceAccount, destinationAccount, status, 0L, null);
    }

    public Transaction(String id,
                       TransactionType typeTransaction,
                       Money amount,
                       LocalDateTime date,
                       LocalDateTime approvalDate,
                       String sourceAccount,
                       String destinationAccount,
                       TransactionStatus status,
                       Long creatorUserId,
                       Long approverUserId) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Invalid transaction id");
        }
        if (creatorUserId == null || creatorUserId < 0) {
            throw new IllegalArgumentException("Invalid creator user id");
        }
        this.id = id;
        this.typeTransaction = typeTransaction;
        this.amount = amount;
        this.date = date;
        this.approvalDate = approvalDate;
        this.sourceAccount = sourceAccount;
        this.destinationAccount = destinationAccount;
        this.status = status;
        this.creatorUserId = creatorUserId;
        this.approverUserId = approverUserId;
    }

    public void approveAndExecute() {
        if (this.status != TransactionStatus.AWAITING_APPROVAL) {
            throw new IllegalStateException("Only transfers awaiting approval can be approved");
        }
        this.status = TransactionStatus.EXECUTED;
    }

    public void approveAndExecute(Long approverUserId) {
        approveAndExecute();
        this.approvalDate = LocalDateTime.now();
        this.approverUserId = approverUserId;
    }

    public void reject() {
        if (this.status != TransactionStatus.AWAITING_APPROVAL) {
            throw new IllegalStateException("Only transfers awaiting approval can be rejected");
        }
        this.status = TransactionStatus.REJECTED;
    }

    public void reject(Long approverUserId) {
        reject();
        this.approverUserId = approverUserId;
    }

    public void expire() {
        if (this.status != TransactionStatus.AWAITING_APPROVAL) {
            throw new IllegalStateException("Only transfers awaiting approval can expire");
        }
        this.status = TransactionStatus.EXPIRED;
    }

    public String getId() {
        return id;
    }

    public TransactionType getTransactionType() {
        return typeTransaction;
    }

    public Money getAmount() {
        return amount;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public LocalDateTime getApprovalDate() {
        return approvalDate;
    }

    public String getSourceAccount() {
        return sourceAccount;
    }

    public String getDestinationAccount() {
        return destinationAccount;
    }

    public Long getCreatorUserId() {
        return creatorUserId;
    }

    public Long getApproverUserId() {
        return approverUserId;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
