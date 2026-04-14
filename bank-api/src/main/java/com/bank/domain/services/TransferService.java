package com.bank.domain.services;

import com.bank.domain.entities.Account;
import com.bank.domain.entities.TransactionStatus;
import com.bank.domain.entities.TransactionType;
import com.bank.domain.entities.Transaction;
import com.bank.domain.valueobjects.Money;

public class TransferService {

    public Transaction transfer(Account source, Account destination, Money amount, boolean requiresApproval, Long creatorUserId) {
        if (source.getAccountNumber().equals(destination.getAccountNumber())) {
            throw new IllegalArgumentException("Source and destination accounts cannot be the same");
        }

        TransactionStatus initialStatus = requiresApproval
                ? TransactionStatus.AWAITING_APPROVAL
                : TransactionStatus.EXECUTED;

        if (!requiresApproval) {
            source.withdraw(amount);
            destination.deposit(amount);
        }

        return new Transaction(
                TransactionType.TRANSFER,
                amount,
                source.getAccountNumber().value(),
                destination.getAccountNumber().value(),
                initialStatus,
                creatorUserId
        );
    }

    public void executePendingTransfer(Transaction transaction, Account source, Account destination, Long approverUserId) {
        if (transaction.getStatus() != TransactionStatus.AWAITING_APPROVAL) {
            throw new IllegalStateException("Only transfers awaiting approval can be executed");
        }
        source.withdraw(transaction.getAmount());
        destination.deposit(transaction.getAmount());
        transaction.approveAndExecute(approverUserId);
    }
}
