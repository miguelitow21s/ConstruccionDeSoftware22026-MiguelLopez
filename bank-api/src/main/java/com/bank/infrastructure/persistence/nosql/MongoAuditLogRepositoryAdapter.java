package com.bank.infrastructure.persistence.nosql;

import java.util.List;
import java.util.Objects;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.bank.application.ports.AuditLogEntry;
import com.bank.application.ports.AuditLogRepositoryPort;

@Component
@Primary
@ConditionalOnProperty(prefix = "bank.auditLog", name = "storage", havingValue = "mongodb")
public class MongoAuditLogRepositoryAdapter implements AuditLogRepositoryPort {

    private final MongoAuditLogSpringRepository repository;

    public MongoAuditLogRepositoryAdapter(MongoAuditLogSpringRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(AuditLogEntry entry) {
        repository.save(Objects.requireNonNull(toDocument(entry)));
    }

    @Override
    public List<AuditLogEntry> findAll() {
        return repository.findAll().stream().map(this::toEntry).toList();
    }

    @Override
    public List<AuditLogEntry> findByUserId(String userId) {
        return repository.findByUserId(userId).stream().map(this::toEntry).toList();
    }

    @Override
    public List<AuditLogEntry> findByAffectedProductIdIn(List<String> affectedProductIds) {
        return repository.findByAffectedProductIdIn(affectedProductIds).stream().map(this::toEntry).toList();
    }

    private AuditLogDocument toDocument(AuditLogEntry entry) {
        AuditLogDocument doc = new AuditLogDocument();
        doc.setIdAuditLog(entry.idAuditLog());
        doc.setOperationType(entry.operationType());
        doc.setOperationDateTime(entry.operationDateTime());
        doc.setUserId(entry.userId());
        doc.setUserRole(entry.userRole());
        doc.setAffectedProductId(entry.affectedProductId());
        doc.setDetailData(entry.detailData());
        return doc;
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

