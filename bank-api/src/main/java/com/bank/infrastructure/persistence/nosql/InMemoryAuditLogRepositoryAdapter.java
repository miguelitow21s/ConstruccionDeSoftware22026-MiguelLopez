package com.bank.infrastructure.persistence.nosql;

import com.bank.application.ports.AuditLogEntry;
import com.bank.application.ports.AuditLogRepositoryPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(prefix = "bank.auditLog", name = "storage", havingValue = "memory", matchIfMissing = true)
public class InMemoryAuditLogRepositoryAdapter implements AuditLogRepositoryPort {

    private final List<AuditLogDocument> store = new ArrayList<>();

    @Override
    public void save(AuditLogEntry entry) {
        AuditLogDocument doc = new AuditLogDocument();
        doc.setIdAuditLog(entry.idAuditLog());
        doc.setTypeOperacion(entry.typeOperacion());
        doc.setDateHoraOperacion(entry.operationDateTime());
        doc.setUserId(entry.userId());
        doc.setRoleUser(entry.userRole());
        doc.setIdProductoAfectado(entry.idProductoAfectado());
        doc.setDatosDetalle(entry.datosDetalle());
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
    public List<AuditLogEntry> findByIdProductoAfectadoIn(List<String> idsProductoAfectado) {
        if (idsProductoAfectado == null || idsProductoAfectado.isEmpty()) {
            return List.of();
        }
        Set<String> ids = Set.copyOf(idsProductoAfectado);
        return store.stream()
                .filter(doc -> doc.getIdProductoAfectado() != null && ids.contains(doc.getIdProductoAfectado()))
                .map(this::toEntry)
                .collect(Collectors.toList());
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
