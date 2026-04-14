package com.bank.infrastructure.persistence.nosql;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "auditLog")
public class AuditLogDocument {

    @Id
    private String idAuditLog;
    private String operationType;
    private LocalDateTime operationDateTime;

    @Indexed
    private String userId;
    private String userRole;

    @Indexed
    private String affectedProductId;
    private Map<String, Object> detailData;

    public String getIdAuditLog() {
        return idAuditLog;
    }

    public void setIdAuditLog(String idAuditLog) {
        this.idAuditLog = idAuditLog;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public LocalDateTime getOperationDateTime() {
        return operationDateTime;
    }

    public void setOperationDateTime(LocalDateTime operationDateTime) {
        this.operationDateTime = operationDateTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public String getAffectedProductId() {
        return affectedProductId;
    }

    public void setAffectedProductId(String affectedProductId) {
        this.affectedProductId = affectedProductId;
    }

    public Map<String, Object> getDetailData() {
        return detailData;
    }

    public void setDetailData(Map<String, Object> detailData) {
        this.detailData = detailData;
    }
}

