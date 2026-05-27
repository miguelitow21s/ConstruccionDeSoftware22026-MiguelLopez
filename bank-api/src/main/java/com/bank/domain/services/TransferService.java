package com.bank.domain.services;

import com.bank.domain.entities.Account;
import com.bank.domain.entities.TransactionStatus;
import com.bank.domain.entities.TransactionType;
import com.bank.domain.entities.Transaction;
import com.bank.domain.valueobjects.Money;

// Servicio de dominio que orquesta la lógica de transferencia entre dos cuentas.
// Está en el dominio porque toca múltiples entidades (dos cuentas + la transacción)
// y la lógica pertenece al negocio, no a la infraestructura.
public class TransferService {

    public Transaction transfer(Account source, Account destination, Money amount, boolean requiresApproval, Long creatorUserId) {
        if (source.getAccountNumber().equals(destination.getAccountNumber())) {
            throw new IllegalArgumentException("Source and destination accounts cannot be the same");
        }

        TransactionStatus initialStatus = requiresApproval
                ? TransactionStatus.AWAITING_APPROVAL
                : TransactionStatus.EXECUTED;

        // Si no requiere aprobación, movemos el dinero de inmediato.
        // Si requiere aprobación, el dinero se mueve cuando el supervisor apruebe.
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

    // Ejecutar una transferencia que estaba esperando aprobación del supervisor
    public void executePendingTransfer(Transaction transaction, Account source, Account destination, Long approverUserId) {
        if (transaction.getStatus() != TransactionStatus.AWAITING_APPROVAL) {
            throw new IllegalStateException("Only transfers awaiting approval can be executed");
        }
        source.withdraw(transaction.getAmount());
        destination.deposit(transaction.getAmount());
        transaction.approveAndExecute(approverUserId);
    }
}
