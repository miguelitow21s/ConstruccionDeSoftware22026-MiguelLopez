package com.bank.domain.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.bank.domain.valueobjects.Money;

class TransactionTest {

    @Test
    void debePermitirAprobarYEjecutarSoloSiEstaEnEspera() {
        Transaction transaction = transaction(TransactionStatus.AWAITING_APPROVAL);

        transaction.approveAndExecute();

        assertEquals(TransactionStatus.EXECUTED, transaction.getStatus());
    }

    @Test
    void debeFallarAprobarYEjecutarSiNoEstaEnEspera() {
        Transaction transaction = transaction(TransactionStatus.PENDING);

        IllegalStateException thrown = assertThrows(IllegalStateException.class, transaction::approveAndExecute);
        assertEquals("Only transfers awaiting approval can be approved", thrown.getMessage());
    }

    @Test
    void debeFallarRechazarSiNoEstaEnEspera() {
        Transaction transaction = transaction(TransactionStatus.EXECUTED);

        IllegalStateException thrown = assertThrows(IllegalStateException.class, transaction::reject);
        assertEquals("Only transfers awaiting approval can be rejected", thrown.getMessage());
    }

    @Test
    void debeFallarVencerSiNoEstaEnEspera() {
        Transaction transaction = transaction(TransactionStatus.REJECTED);

        IllegalStateException thrown = assertThrows(IllegalStateException.class, transaction::expire);
        assertEquals("Only transfers awaiting approval can expire", thrown.getMessage());
    }

    private Transaction transaction(TransactionStatus status) {
        return new Transaction(
                "tx-1",
                TransactionType.TRANSFER,
                Money.positive(BigDecimal.valueOf(100)),
                LocalDateTime.now(),
                "10000111",
                "10000112",
                status
        );
    }
}
