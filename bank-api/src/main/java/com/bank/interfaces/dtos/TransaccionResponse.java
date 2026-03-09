package com.bank.interfaces.dtos;

import com.bank.domain.entities.EstadoTransaccion;
import com.bank.domain.entities.TipoTransaccion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransaccionResponse(
        String id,
        TipoTransaccion tipoTransaccion,
        BigDecimal monto,
        LocalDateTime fecha,
        String cuentaOrigen,
        String cuentaDestino,
        EstadoTransaccion estado
) {
}
