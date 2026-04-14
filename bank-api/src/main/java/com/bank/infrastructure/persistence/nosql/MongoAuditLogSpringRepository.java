package com.bank.infrastructure.persistence.nosql;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MongoAuditLogSpringRepository extends MongoRepository<AuditLogDocument, String> {

    List<AuditLogDocument> findByUserId(String userId);

    List<AuditLogDocument> findByIdProductoAfectadoIn(List<String> idsProductoAfectado);
}
