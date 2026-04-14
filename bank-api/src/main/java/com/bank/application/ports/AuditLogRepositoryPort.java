package com.bank.application.ports;

import java.util.List;

public interface AuditLogRepositoryPort {

    void save(AuditLogEntry entry);

    List<AuditLogEntry> findAll();

    List<AuditLogEntry> findByUserId(String userId);

    List<AuditLogEntry> findByAffectedProductIdIn(List<String> affectedProductIds);
}

