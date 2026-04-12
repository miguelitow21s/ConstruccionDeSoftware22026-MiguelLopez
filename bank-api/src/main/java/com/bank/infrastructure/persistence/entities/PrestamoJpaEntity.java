package com.bank.infrastructure.persistence.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.bank.domain.entities.EstadoPrestamo;
import com.bank.domain.entities.TipoPrestamo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "prestamos")
public class PrestamoJpaEntity {

    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPrestamo tipoPrestamo;

    @Column(nullable = false)
    private String clienteSolicitanteId;

    @Column(nullable = false, length = 20)
    private String clienteSolicitanteIdentificacion;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal montoSolicitado;

    @Column(precision = 19, scale = 2)
    private BigDecimal montoAprobado;

    @Column(nullable = false, precision = 9, scale = 4)
    private BigDecimal tasaInteres;

    @Column(nullable = false)
    private Integer plazoMeses;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPrestamo estado;

    private LocalDateTime fechaAprobacion;

    private LocalDateTime fechaDesembolso;

    @Column(length = 20)
    private String cuentaDestinoDesembolso;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TipoPrestamo getTipoPrestamo() {
        return tipoPrestamo;
    }

    public void setTipoPrestamo(TipoPrestamo tipoPrestamo) {
        this.tipoPrestamo = tipoPrestamo;
    }

    public String getClienteSolicitanteId() {
        return clienteSolicitanteId;
    }

    public void setClienteSolicitanteId(String clienteSolicitanteId) {
        this.clienteSolicitanteId = clienteSolicitanteId;
    }

    public String getClienteSolicitanteIdentificacion() {
        return clienteSolicitanteIdentificacion;
    }

    public void setClienteSolicitanteIdentificacion(String clienteSolicitanteIdentificacion) {
        this.clienteSolicitanteIdentificacion = clienteSolicitanteIdentificacion;
    }

    public BigDecimal getMontoSolicitado() {
        return montoSolicitado;
    }

    public void setMontoSolicitado(BigDecimal montoSolicitado) {
        this.montoSolicitado = montoSolicitado;
    }

    public BigDecimal getMontoAprobado() {
        return montoAprobado;
    }

    public void setMontoAprobado(BigDecimal montoAprobado) {
        this.montoAprobado = montoAprobado;
    }

    public BigDecimal getTasaInteres() {
        return tasaInteres;
    }

    public void setTasaInteres(BigDecimal tasaInteres) {
        this.tasaInteres = tasaInteres;
    }

    public Integer getPlazoMeses() {
        return plazoMeses;
    }

    public void setPlazoMeses(Integer plazoMeses) {
        this.plazoMeses = plazoMeses;
    }

    public EstadoPrestamo getEstado() {
        return estado;
    }

    public void setEstado(EstadoPrestamo estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaAprobacion() {
        return fechaAprobacion;
    }

    public void setFechaAprobacion(LocalDateTime fechaAprobacion) {
        this.fechaAprobacion = fechaAprobacion;
    }

    public LocalDateTime getFechaDesembolso() {
        return fechaDesembolso;
    }

    public void setFechaDesembolso(LocalDateTime fechaDesembolso) {
        this.fechaDesembolso = fechaDesembolso;
    }

    public String getCuentaDestinoDesembolso() {
        return cuentaDestinoDesembolso;
    }

    public void setCuentaDestinoDesembolso(String cuentaDestinoDesembolso) {
        this.cuentaDestinoDesembolso = cuentaDestinoDesembolso;
    }
}
