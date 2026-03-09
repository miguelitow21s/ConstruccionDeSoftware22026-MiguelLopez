package com.bank.infrastructure.persistence.nosql;

import java.time.LocalDateTime;
import java.util.Map;

public class BitacoraDocument {

    private String idBitacora;
    private String tipoOperacion;
    private LocalDateTime fechaHoraOperacion;
    private String idUsuario;
    private String rolUsuario;
    private String idProductoAfectado;
    private Map<String, Object> datosDetalle;

    public String getIdBitacora() {
        return idBitacora;
    }

    public void setIdBitacora(String idBitacora) {
        this.idBitacora = idBitacora;
    }

    public String getTipoOperacion() {
        return tipoOperacion;
    }

    public void setTipoOperacion(String tipoOperacion) {
        this.tipoOperacion = tipoOperacion;
    }

    public LocalDateTime getFechaHoraOperacion() {
        return fechaHoraOperacion;
    }

    public void setFechaHoraOperacion(LocalDateTime fechaHoraOperacion) {
        this.fechaHoraOperacion = fechaHoraOperacion;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getRolUsuario() {
        return rolUsuario;
    }

    public void setRolUsuario(String rolUsuario) {
        this.rolUsuario = rolUsuario;
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
