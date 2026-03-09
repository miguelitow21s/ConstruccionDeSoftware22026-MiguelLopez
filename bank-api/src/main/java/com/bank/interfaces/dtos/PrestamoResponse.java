package com.bank.interfaces.dtos;

import com.bank.domain.entities.EstadoPrestamo;
import com.bank.domain.entities.TipoPrestamo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PrestamoResponse(
        String id,
        TipoPrestamo tipoPrestamo,
        String clienteSolicitanteId,
        BigDecimal montoSolicitado,
        BigDecimal montoAprobado,
        BigDecimal tasaInteres,
        int plazoMeses,
        EstadoPrestamo estado,
        LocalDateTime fechaAprobacion,
        LocalDateTime fechaDesembolso,
        String cuentaDestinoDesembolso
) {
}
