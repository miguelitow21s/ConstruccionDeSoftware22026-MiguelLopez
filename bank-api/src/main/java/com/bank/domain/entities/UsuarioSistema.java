package com.bank.domain.entities;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;
import java.util.regex.Pattern;

import com.bank.domain.valueobjects.Email;

public class UsuarioSistema {

    private static final int MAX_NOMBRE = 100;
    private static final int MAX_IDENTIFICACION = 20;
    private static final int MAX_CORREO = 100;
    private static final int MAX_TELEFONO = 15;
    private static final int MIN_TELEFONO = 7;
    private static final int MAX_DIRECCION = 200;
    private static final Pattern TELEFONO_PATTERN = Pattern.compile("^\\d{7,15}$");

    private final Long idUsuario;
    private final String idRelacionado;
    private final String nombreCompleto;
    private final String idIdentificacion;
    private final Email correoElectronico;
    private final String telefono;
    private final LocalDate fechaNacimiento;
    private final String direccion;
    private final RolSistema rolSistema;
    private EstadoUsuario estadoUsuario;

    public UsuarioSistema(Long idUsuario,
                          String idRelacionado,
                          String nombreCompleto,
                          String idIdentificacion,
                          Email correoElectronico,
                          String telefono,
                          LocalDate fechaNacimiento,
                          String direccion,
                          RolSistema rolSistema,
                          EstadoUsuario estadoUsuario) {
                validarCampos(idUsuario, idRelacionado, nombreCompleto, idIdentificacion, correoElectronico, telefono, fechaNacimiento, direccion, rolSistema, estadoUsuario);
        this.idUsuario = idUsuario;
        this.idRelacionado = idRelacionado;
        this.nombreCompleto = nombreCompleto;
        this.idIdentificacion = idIdentificacion;
        this.correoElectronico = correoElectronico;
        this.telefono = telefono;
        this.fechaNacimiento = fechaNacimiento;
        this.direccion = direccion;
        this.rolSistema = rolSistema;
        this.estadoUsuario = estadoUsuario;
    }

    public void bloquear() {
        this.estadoUsuario = EstadoUsuario.BLOQUEADO;
    }

    public void inactivar() {
        this.estadoUsuario = EstadoUsuario.INACTIVO;
    }

    public void activar() {
        this.estadoUsuario = EstadoUsuario.ACTIVO;
    }

    private void validarCampos(Long idUsuario,
                               String idRelacionado,
                               String nombreCompleto,
                               String idIdentificacion,
                               Email correoElectronico,
                               String telefono,
                               LocalDate fechaNacimiento,
                               String direccion,
                               RolSistema rolSistema,
                               EstadoUsuario estadoUsuario) {
        if (idUsuario == null || idUsuario <= 0) {
            throw new IllegalArgumentException("ID de usuario invalido");
        }
        if (nombreCompleto == null || nombreCompleto.isBlank() || nombreCompleto.length() > MAX_NOMBRE) {
            throw new IllegalArgumentException("Nombre completo obligatorio");
        }
        if (idIdentificacion == null || idIdentificacion.isBlank() || idIdentificacion.length() > MAX_IDENTIFICACION) {
            throw new IllegalArgumentException("Identificacion obligatoria");
        }
        if (correoElectronico == null || correoElectronico.value().length() > MAX_CORREO) {
            throw new IllegalArgumentException("Correo electronico obligatorio y valido");
        }
        if (telefono == null || telefono.length() < MIN_TELEFONO || telefono.length() > MAX_TELEFONO || !TELEFONO_PATTERN.matcher(telefono).matches()) {
            throw new IllegalArgumentException("Telefono invalido");
        }
        if (direccion == null || direccion.isBlank() || direccion.length() > MAX_DIRECCION) {
            throw new IllegalArgumentException("Direccion obligatoria");
        }
        if (rolSistema == null) {
            throw new IllegalArgumentException("Rol obligatorio");
        }
        if (estadoUsuario == null) {
            throw new IllegalArgumentException("Estado de usuario obligatorio");
        }

        if (requiereIdRelacionado(rolSistema) && (idRelacionado == null || idRelacionado.isBlank())) {
            throw new IllegalArgumentException("ID relacionado obligatorio para el rol " + rolSistema);
        }

        if (esPersonaNatural(rolSistema)) {
            if (fechaNacimiento == null) {
                throw new IllegalArgumentException("Fecha de nacimiento obligatoria para persona natural");
            }
            int edad = Period.between(fechaNacimiento, LocalDate.now()).getYears();
            if (edad < 18) {
                throw new IllegalArgumentException("El usuario debe ser mayor de edad");
            }
        }
    }

    private boolean esPersonaNatural(RolSistema rolSistema) {
        return rolSistema == RolSistema.CLIENTE_PERSONA_NATURAL;
    }

    private boolean requiereIdRelacionado(RolSistema rolSistema) {
        return rolSistema != RolSistema.ANALISTA_INTERNO
                && rolSistema != RolSistema.EMPLEADO_VENTANILLA
                && rolSistema != RolSistema.EMPLEADO_COMERCIAL
                && rolSistema != RolSistema.EMPLEADO_EMPRESA;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public String getIdRelacionado() {
        return idRelacionado;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getIdIdentificacion() {
        return idIdentificacion;
    }

    public Email getCorreoElectronico() {
        return correoElectronico;
    }

    public String getTelefono() {
        return telefono;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public String getDireccion() {
        return direccion;
    }

    public RolSistema getRolSistema() {
        return rolSistema;
    }

    public EstadoUsuario getEstadoUsuario() {
        return estadoUsuario;
    }

    public boolean estaActivo() {
        return estadoUsuario == EstadoUsuario.ACTIVO;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UsuarioSistema that = (UsuarioSistema) o;
        return Objects.equals(idUsuario, that.idUsuario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUsuario);
    }
}
