package com.bank.domain.entities;

import java.util.Objects;
import java.util.UUID;

import com.bank.domain.valueobjects.Email;

public class Cliente {

    private final String id;
    private final String idIdentificacion;
    private final String nombre;
    private final Email email;
    private final String telefono;
    private final TipoCliente tipoCliente;
    private final String representanteLegalId;

    public Cliente(String idIdentificacion, String nombre, Email email, String telefono) {
        this(UUID.randomUUID().toString(), idIdentificacion, nombre, email, telefono, TipoCliente.CLIENTE_PERSONA_NATURAL, null);
    }

    public Cliente(String idIdentificacion,
                   String nombre,
                   Email email,
                   String telefono,
                   TipoCliente tipoCliente,
                   String representanteLegalId) {
        this(UUID.randomUUID().toString(), idIdentificacion, nombre, email, telefono, tipoCliente, representanteLegalId);
    }

    public Cliente(String id, String idIdentificacion, String nombre, Email email, String telefono) {
        this(id, idIdentificacion, nombre, email, telefono, TipoCliente.CLIENTE_PERSONA_NATURAL, null);
    }

    public Cliente(String id,
                   String idIdentificacion,
                   String nombre,
                   Email email,
                   String telefono,
                   TipoCliente tipoCliente,
                   String representanteLegalId) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Id de cliente invalido");
        }
        if (idIdentificacion == null || idIdentificacion.isBlank() || idIdentificacion.length() > 20) {
            throw new IllegalArgumentException("Identificacion invalida");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("Nombre de cliente invalido");
        }
        if (telefono == null || telefono.length() < 7 || telefono.length() > 15) {
            throw new IllegalArgumentException("Telefono invalido");
        }
        if (tipoCliente == null) {
            throw new IllegalArgumentException("Tipo de cliente obligatorio");
        }
        if (tipoCliente == TipoCliente.CLIENTE_EMPRESA
                && (representanteLegalId == null || representanteLegalId.isBlank())) {
            throw new IllegalArgumentException("Representante legal obligatorio para cliente empresa");
        }
        this.id = id;
        this.idIdentificacion = idIdentificacion;
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.tipoCliente = tipoCliente;
        this.representanteLegalId = representanteLegalId;
    }

    public String getId() {
        return id;
    }

    public String getIdIdentificacion() {
        return idIdentificacion;
    }

    public String getNombre() {
        return nombre;
    }

    public Email getEmail() {
        return email;
    }

    public String getTelefono() {
        return telefono;
    }

    public TipoCliente getTipoCliente() {
        return tipoCliente;
    }

    public String getRepresentanteLegalId() {
        return representanteLegalId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Cliente cliente = (Cliente) o;
        return Objects.equals(id, cliente.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
