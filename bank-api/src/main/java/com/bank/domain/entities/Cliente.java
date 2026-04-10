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

    public Cliente(String idIdentificacion, String nombre, Email email, String telefono) {
        this(UUID.randomUUID().toString(), idIdentificacion, nombre, email, telefono);
    }

    public Cliente(String id, String idIdentificacion, String nombre, Email email, String telefono) {
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
        this.id = id;
        this.idIdentificacion = idIdentificacion;
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
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
