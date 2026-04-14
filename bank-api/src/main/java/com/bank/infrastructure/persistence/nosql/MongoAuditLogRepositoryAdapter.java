package com.bank.infrastructure.persistence.nosql;

import com.bank.application.ports.AuditLogEntry;
import com.bank.application.ports.AuditLogRepositoryPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

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
    public List<AuditLogEntry> findByIdProductoAfectadoIn(List<String> idsProductoAfectado) {
        return repository.findByIdProductoAfectadoIn(idsProductoAfectado).stream().map(this::toEntry).toList();
    }

    private AuditLogDocument toDocument(AuditLogEntry entry) {
        AuditLogDocument doc = new AuditLogDocument();
        doc.setIdAuditLog(entry.idAuditLog());
        doc.setTypeOperacion(entry.typeOperacion());
        doc.setDateHoraOperacion(entry.operationDateTime());
        doc.setUserId(entry.userId());
        doc.setRoleUser(entry.userRole());
        doc.setIdProductoAfectado(entry.idProductoAfectado());
        doc.setDatosDetalle(entry.datosDetalle());
        return doc;
    }

    private AuditLogEntry toEntry(AuditLogDocument doc) {
        return new AuditLogEntry(
                doc.getIdAuditLog(),
                doc.getTypeOperacion(),
                doc.getDateHoraOperacion(),
                doc.getUserId(),
                doc.getRoleUser(),
                doc.getIdProductoAfectado(),
                doc.getDatosDetalle()
        );
    }
}
