package com.bank.application.ports;

import java.time.LocalDateTime;
import java.util.Map;

public record AuditLogEntry(
        String idAuditLog,
        String operationType,
        LocalDateTime operationDateTime,
        String userId,
        String userRole,
        String affectedProductId,
        Map<String, Object> detailData
) {
}

