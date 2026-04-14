package com.bank.infrastructure.persistence.nosql;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.bank.application.ports.AuditLogEntry;
import com.bank.application.ports.AuditLogRepositoryPort;

@Component
@ConditionalOnProperty(prefix = "bank.auditLog", name = "storage", havingValue = "memory", matchIfMissing = true)
public class InMemoryAuditLogRepositoryAdapter implements AuditLogRepositoryPort {

    private final List<AuditLogDocument> store = new ArrayList<>();

    @Override
    public void save(AuditLogEntry entry) {
        AuditLogDocument doc = new AuditLogDocument();
        doc.setIdAuditLog(entry.idAuditLog());
        doc.setOperationType(entry.operationType());
        doc.setOperationDateTime(entry.operationDateTime());
        doc.setUserId(entry.userId());
        doc.setUserRole(entry.userRole());
        doc.setAffectedProductId(entry.affectedProductId());
        doc.setDetailData(entry.detailData());
        store.add(doc);
    }

    @Override
    public List<AuditLogEntry> findAll() {
        return store.stream().map(this::toEntry).collect(Collectors.toList());
    }

    @Override
    public List<AuditLogEntry> findByUserId(String userId) {
        return store.stream()
                .filter(doc -> doc.getUserId() != null && doc.getUserId().equals(userId))
                .map(this::toEntry)
                .collect(Collectors.toList());
    }

    @Override
    public List<AuditLogEntry> findByAffectedProductIdIn(List<String> affectedProductIds) {
        if (affectedProductIds == null || affectedProductIds.isEmpty()) {
            return List.of();
        }
        Set<String> ids = Set.copyOf(affectedProductIds);
        return store.stream()
                .filter(doc -> doc.getAffectedProductId() != null && ids.contains(doc.getAffectedProductId()))
                .map(this::toEntry)
                .collect(Collectors.toList());
    }

    private AuditLogEntry toEntry(AuditLogDocument doc) {
        return new AuditLogEntry(
                doc.getIdAuditLog(),
            doc.getOperationType(),
            doc.getOperationDateTime(),
                doc.getUserId(),
            doc.getUserRole(),
            doc.getAffectedProductId(),
            doc.getDetailData()
        );
    }
}

