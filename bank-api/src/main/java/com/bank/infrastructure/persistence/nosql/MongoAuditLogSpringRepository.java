package com.bank.infrastructure.persistence.nosql;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoAuditLogSpringRepository extends MongoRepository<AuditLogDocument, String> {

    List<AuditLogDocument> findByUserId(String userId);

    List<AuditLogDocument> findByAffectedProductIdIn(List<String> affectedProductIds);
}

