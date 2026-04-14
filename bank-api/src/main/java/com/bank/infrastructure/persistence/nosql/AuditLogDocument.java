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
    private String typeOperacion;
    private LocalDateTime operationDateTime;

    @Indexed
    private String userId;
    private String userRole;

    @Indexed
    private String idProductoAfectado;
    private Map<String, Object> datosDetalle;

    public String getIdAuditLog() {
        return idAuditLog;
    }

    public void setIdAuditLog(String idAuditLog) {
        this.idAuditLog = idAuditLog;
    }

    public String getTypeOperacion() {
        return typeOperacion;
    }

    public void setTypeOperacion(String typeOperacion) {
        this.typeOperacion = typeOperacion;
    }

    public LocalDateTime getDateHoraOperacion() {
        return operationDateTime;
    }

    public void setDateHoraOperacion(LocalDateTime operationDateTime) {
        this.operationDateTime = operationDateTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRoleUser() {
        return userRole;
    }

    public void setRoleUser(String userRole) {
        this.userRole = userRole;
    }

    public String getIdProductoAfectado() {
        return idProductoAfectado;
    }

    public void setIdProductoAfectado(String idProductoAfectado) {
        this.idProductoAfectado = idProductoAfectado;
    }

    public Map<String, Object> getDatosDetalle() {
        return datosDetalle;
    }

    public void setDatosDetalle(Map<String, Object> datosDetalle) {
        this.datosDetalle = datosDetalle;
    }
}
