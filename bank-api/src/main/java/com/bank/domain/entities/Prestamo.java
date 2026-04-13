package com.bank.domain.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Prestamo {

    private final String id;
    private final TipoPrestamo tipoPrestamo;
    private final String clienteSolicitanteId;
    private final String clienteSolicitanteIdentificacion;
    private final BigDecimal montoSolicitado;
    private BigDecimal montoAprobado;
    private final BigDecimal tasaInteres;
    private final int plazoMeses;
    private EstadoPrestamo estado;
    private LocalDateTime fechaAprobacion;
    private LocalDateTime fechaDesembolso;
    private String cuentaDestinoDesembolso;

    public Prestamo(TipoPrestamo tipoPrestamo,
                    String clienteSolicitanteId,
                    String clienteSolicitanteIdentificacion,
                    BigDecimal montoSolicitado,
                    BigDecimal tasaInteres,
                    int plazoMeses) {
        this(
                UUID.randomUUID().toString(),
                tipoPrestamo,
                clienteSolicitanteId,
                clienteSolicitanteIdentificacion,
                montoSolicitado,
                null,
                tasaInteres,
                plazoMeses,
                EstadoPrestamo.EN_ESTUDIO,
                null,
                null,
                null
        );
    }

    public Prestamo(TipoPrestamo tipoPrestamo,
                    String clienteSolicitanteId,
                    BigDecimal montoSolicitado,
                    BigDecimal tasaInteres,
                    int plazoMeses) {
        this(tipoPrestamo, clienteSolicitanteId, clienteSolicitanteId, montoSolicitado, tasaInteres, plazoMeses);
    }

    public Prestamo(String id,
                    TipoPrestamo tipoPrestamo,
                    String clienteSolicitanteId,
                BigDecimal montoSolicitado,
                BigDecimal montoAprobado,
                BigDecimal tasaInteres,
                int plazoMeses,
                EstadoPrestamo estado,
                LocalDateTime fechaAprobacion,
                LocalDateTime fechaDesembolso,
                String cuentaDestinoDesembolso) {
        this(
            id,
            tipoPrestamo,
            clienteSolicitanteId,
            clienteSolicitanteId,
            montoSolicitado,
            montoAprobado,
            tasaInteres,
            plazoMeses,
            estado,
            fechaAprobacion,
            fechaDesembolso,
            cuentaDestinoDesembolso
        );
        }

        public Prestamo(String id,
                TipoPrestamo tipoPrestamo,
                String clienteSolicitanteId,
                    String clienteSolicitanteIdentificacion,
                    BigDecimal montoSolicitado,
                    BigDecimal montoAprobado,
                    BigDecimal tasaInteres,
                    int plazoMeses,
                    EstadoPrestamo estado,
                    LocalDateTime fechaAprobacion,
                    LocalDateTime fechaDesembolso,
                    String cuentaDestinoDesembolso) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Id de prestamo invalido");
        }
        if (clienteSolicitanteId == null || clienteSolicitanteId.isBlank()) {
            throw new IllegalArgumentException("Cliente solicitante invalido");
        }
        if (clienteSolicitanteIdentificacion == null
                || clienteSolicitanteIdentificacion.isBlank()
                || clienteSolicitanteIdentificacion.length() > 20) {
            throw new IllegalArgumentException("Identificacion de cliente solicitante invalida");
        }
        if (montoSolicitado == null || montoSolicitado.signum() <= 0) {
            throw new IllegalArgumentException("El monto solicitado debe ser mayor a cero");
        }
        if (tasaInteres == null || tasaInteres.signum() <= 0) {
            throw new IllegalArgumentException("La tasa de interes debe ser mayor a cero");
        }
        if (plazoMeses <= 0) {
            throw new IllegalArgumentException("El plazo en meses debe ser mayor a cero");
        }
        this.id = id;
        this.tipoPrestamo = tipoPrestamo;
        this.clienteSolicitanteId = clienteSolicitanteId;
        this.clienteSolicitanteIdentificacion = clienteSolicitanteIdentificacion;
        this.montoSolicitado = montoSolicitado;
        this.montoAprobado = montoAprobado;
        this.tasaInteres = tasaInteres;
        this.plazoMeses = plazoMeses;
        this.estado = estado;
        this.fechaAprobacion = fechaAprobacion;
        this.fechaDesembolso = fechaDesembolso;
        this.cuentaDestinoDesembolso = cuentaDestinoDesembolso;

        validarConsistenciaEstado();
    }

    public void aprobar(BigDecimal montoAprobado) {
        if (estado != EstadoPrestamo.EN_ESTUDIO) {
            throw new IllegalStateException("Solo se puede aprobar un prestamo en estudio");
        }
        if (montoAprobado == null || montoAprobado.signum() <= 0) {
            throw new IllegalArgumentException("El monto aprobado debe ser mayor a cero");
        }
        this.montoAprobado = montoAprobado;
        this.estado = EstadoPrestamo.APROBADO;
        this.fechaAprobacion = LocalDateTime.now();
    }

    public void rechazar() {
        if (estado != EstadoPrestamo.EN_ESTUDIO) {
            throw new IllegalStateException("Solo se puede rechazar un prestamo en estudio");
        }
        this.estado = EstadoPrestamo.RECHAZADO;
    }

    public void desembolsar(String numeroCuentaDestino) {
        if (estado != EstadoPrestamo.APROBADO) {
            throw new IllegalStateException("Solo se puede desembolsar un prestamo aprobado");
        }
        if (montoAprobado == null || montoAprobado.signum() <= 0) {
            throw new IllegalStateException("No existe monto aprobado valido para desembolso");
        }
        if (numeroCuentaDestino == null || numeroCuentaDestino.isBlank()) {
            throw new IllegalArgumentException("La cuenta destino de desembolso es obligatoria");
        }
        this.cuentaDestinoDesembolso = numeroCuentaDestino;
        this.estado = EstadoPrestamo.DESEMBOLSADO;
        this.fechaDesembolso = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public TipoPrestamo getTipoPrestamo() {
        return tipoPrestamo;
    }

    public String getClienteSolicitanteId() {
        return clienteSolicitanteId;
    }

    public String getClienteSolicitanteIdentificacion() {
        return clienteSolicitanteIdentificacion;
    }

    public BigDecimal getMontoSolicitado() {
        return montoSolicitado;
    }

    public BigDecimal getMontoAprobado() {
        return montoAprobado;
    }

    public BigDecimal getTasaInteres() {
        return tasaInteres;
    }

    public int getPlazoMeses() {
        return plazoMeses;
    }

    public EstadoPrestamo getEstado() {
        return estado;
    }

    public LocalDateTime getFechaAprobacion() {
        return fechaAprobacion;
    }

    public LocalDateTime getFechaDesembolso() {
        return fechaDesembolso;
    }

    public String getCuentaDestinoDesembolso() {
        return cuentaDestinoDesembolso;
    }

    private void validarConsistenciaEstado() {
        if ((estado == EstadoPrestamo.APROBADO || estado == EstadoPrestamo.DESEMBOLSADO)
                && (montoAprobado == null || montoAprobado.signum() <= 0)) {
            throw new IllegalArgumentException("Monto aprobado obligatorio cuando el prestamo esta aprobado o desembolsado");
        }
        if (estado == EstadoPrestamo.DESEMBOLSADO) {
            if (cuentaDestinoDesembolso == null || cuentaDestinoDesembolso.isBlank()) {
                throw new IllegalArgumentException("Cuenta destino desembolso obligatoria para prestamo desembolsado");
            }
            if (fechaDesembolso == null) {
                throw new IllegalArgumentException("Fecha de desembolso obligatoria para prestamo desembolsado");
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Prestamo prestamo = (Prestamo) o;
        return Objects.equals(id, prestamo.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
