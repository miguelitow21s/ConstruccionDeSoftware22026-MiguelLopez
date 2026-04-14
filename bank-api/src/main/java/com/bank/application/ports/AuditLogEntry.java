package com.bank.application.ports;

import java.time.LocalDateTime;
import java.util.Map;

public record AuditLogEntry(
        String idAuditLog,
        String typeOperacion,
        LocalDateTime operationDateTime,
        String userId,
        String userRole,
        String idProductoAfectado,
        Map<String, Object> datosDetalle
) {
}
