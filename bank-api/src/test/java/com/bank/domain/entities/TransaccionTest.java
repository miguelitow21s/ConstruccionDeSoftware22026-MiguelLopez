package com.bank.domain.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.bank.domain.valueobjects.Dinero;

class TransaccionTest {

    @Test
    void debePermitirAprobarYEjecutarSoloSiEstaEnEspera() {
        Transaccion transaccion = transaccion(EstadoTransaccion.EN_ESPERA_APROBACION);

        transaccion.aprobarYEjecutar();

        assertEquals(EstadoTransaccion.EJECUTADA, transaccion.getEstado());
    }

    @Test
    void debeFallarAprobarYEjecutarSiNoEstaEnEspera() {
        Transaccion transaccion = transaccion(EstadoTransaccion.PENDIENTE);

        IllegalStateException thrown = assertThrows(IllegalStateException.class, transaccion::aprobarYEjecutar);
        assertEquals("Solo se pueden aprobar transferencias en espera de aprobacion", thrown.getMessage());
    }

    @Test
    void debeFallarRechazarSiNoEstaEnEspera() {
        Transaccion transaccion = transaccion(EstadoTransaccion.EJECUTADA);

        IllegalStateException thrown = assertThrows(IllegalStateException.class, transaccion::rechazar);
        assertEquals("Solo se pueden rechazar transferencias en espera de aprobacion", thrown.getMessage());
    }

    @Test
    void debeFallarVencerSiNoEstaEnEspera() {
        Transaccion transaccion = transaccion(EstadoTransaccion.RECHAZADA);

        IllegalStateException thrown = assertThrows(IllegalStateException.class, transaccion::vencer);
        assertEquals("Solo se pueden vencer transferencias en espera de aprobacion", thrown.getMessage());
    }

    private Transaccion transaccion(EstadoTransaccion estado) {
        return new Transaccion(
                "tx-1",
                TipoTransaccion.TRANSFERENCIA,
                Dinero.positivo(BigDecimal.valueOf(100)),
                LocalDateTime.now(),
                "10000111",
                "10000112",
                estado
        );
    }
}
