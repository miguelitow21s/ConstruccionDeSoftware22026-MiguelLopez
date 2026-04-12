package com.bank.domain.entities;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import com.bank.domain.valueobjects.Dinero;

public class Transaccion {

    private final String id;
    private final TipoTransaccion tipoTransaccion;
    private final Dinero monto;
    private final LocalDateTime fecha;
    private LocalDateTime fechaAprobacion;
    private final String cuentaOrigen;
    private final String cuentaDestino;
    private final Long idUsuarioCreador;
    private Long idUsuarioAprobador;
    private EstadoTransaccion estado;

    public Transaccion(TipoTransaccion tipoTransaccion, Dinero monto, String cuentaOrigen, String cuentaDestino, EstadoTransaccion estado) {
        this(UUID.randomUUID().toString(), tipoTransaccion, monto, LocalDateTime.now(), null, cuentaOrigen, cuentaDestino, estado, 0L, null);
    }

    public Transaccion(TipoTransaccion tipoTransaccion,
                       Dinero monto,
                       String cuentaOrigen,
                       String cuentaDestino,
                       EstadoTransaccion estado,
                       Long idUsuarioCreador) {
        this(UUID.randomUUID().toString(), tipoTransaccion, monto, LocalDateTime.now(), null, cuentaOrigen, cuentaDestino, estado, idUsuarioCreador, null);
    }

    public Transaccion(String id, TipoTransaccion tipoTransaccion, Dinero monto, LocalDateTime fecha,
                       String cuentaOrigen, String cuentaDestino, EstadoTransaccion estado) {
        this(id, tipoTransaccion, monto, fecha, null, cuentaOrigen, cuentaDestino, estado, 0L, null);
    }

    public Transaccion(String id,
                       TipoTransaccion tipoTransaccion,
                       Dinero monto,
                       LocalDateTime fecha,
                       LocalDateTime fechaAprobacion,
                       String cuentaOrigen,
                       String cuentaDestino,
                       EstadoTransaccion estado,
                       Long idUsuarioCreador,
                       Long idUsuarioAprobador) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Id de transaccion invalido");
        }
        if (idUsuarioCreador == null || idUsuarioCreador < 0) {
            throw new IllegalArgumentException("ID de usuario creador invalido");
        }
        this.id = id;
        this.tipoTransaccion = tipoTransaccion;
        this.monto = monto;
        this.fecha = fecha;
        this.fechaAprobacion = fechaAprobacion;
        this.cuentaOrigen = cuentaOrigen;
        this.cuentaDestino = cuentaDestino;
        this.estado = estado;
        this.idUsuarioCreador = idUsuarioCreador;
        this.idUsuarioAprobador = idUsuarioAprobador;
    }

    public void aprobarYEjecutar() {
        if (this.estado != EstadoTransaccion.EN_ESPERA_APROBACION) {
            throw new IllegalStateException("Solo se pueden aprobar transferencias en espera de aprobacion");
        }
        this.estado = EstadoTransaccion.EJECUTADA;
    }

    public void aprobarYEjecutar(Long idUsuarioAprobador) {
        aprobarYEjecutar();
        this.fechaAprobacion = LocalDateTime.now();
        this.idUsuarioAprobador = idUsuarioAprobador;
    }

    public void rechazar() {
        if (this.estado != EstadoTransaccion.EN_ESPERA_APROBACION) {
            throw new IllegalStateException("Solo se pueden rechazar transferencias en espera de aprobacion");
        }
        this.estado = EstadoTransaccion.RECHAZADA;
    }

    public void rechazar(Long idUsuarioAprobador) {
        rechazar();
        this.idUsuarioAprobador = idUsuarioAprobador;
    }

    public void vencer() {
        if (this.estado != EstadoTransaccion.EN_ESPERA_APROBACION) {
            throw new IllegalStateException("Solo se pueden vencer transferencias en espera de aprobacion");
        }
        this.estado = EstadoTransaccion.VENCIDA;
    }

    public String getId() {
        return id;
    }

    public TipoTransaccion getTipoTransaccion() {
        return tipoTransaccion;
    }

    public Dinero getMonto() {
        return monto;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public LocalDateTime getFechaAprobacion() {
        return fechaAprobacion;
    }

    public String getCuentaOrigen() {
        return cuentaOrigen;
    }

    public String getCuentaDestino() {
        return cuentaDestino;
    }

    public Long getIdUsuarioCreador() {
        return idUsuarioCreador;
    }

    public Long getIdUsuarioAprobador() {
        return idUsuarioAprobador;
    }

    public EstadoTransaccion getEstado() {
        return estado;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Transaccion that = (Transaccion) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
