package com.bank.interfaces.dtos;

import java.time.LocalDateTime;
import java.util.Map;

public record AuditLogResponse(
        String idAuditLog,
        String operationType,
        LocalDateTime operationDateTime,
        String userId,
        String userRole,
        String affectedProductId,
        Map<String, Object> detailData
) {
}
