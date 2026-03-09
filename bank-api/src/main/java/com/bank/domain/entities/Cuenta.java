package com.bank.domain.entities;

import java.util.Objects;
import java.util.UUID;

import com.bank.domain.valueobjects.Dinero;
import com.bank.domain.valueobjects.NumeroCuenta;

public class Cuenta {

    private final String id;
    private final NumeroCuenta numeroCuenta;
    private Dinero saldo;
    private final TipoCuenta tipoCuenta;
    private final String clienteId;
    private EstadoCuenta estado;

    public Cuenta(NumeroCuenta numeroCuenta, Dinero saldo, TipoCuenta tipoCuenta, String clienteId) {
        this(UUID.randomUUID().toString(), numeroCuenta, saldo, tipoCuenta, clienteId, EstadoCuenta.ACTIVA);
    }

    public Cuenta(String id, NumeroCuenta numeroCuenta, Dinero saldo, TipoCuenta tipoCuenta, String clienteId, EstadoCuenta estado) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Id de cuenta invalido");
        }
        if (clienteId == null || clienteId.isBlank()) {
            throw new IllegalArgumentException("Cliente asociado invalido");
        }
        this.id = id;
        this.numeroCuenta = numeroCuenta;
        this.saldo = saldo;
        this.tipoCuenta = tipoCuenta;
        this.clienteId = clienteId;
        this.estado = estado;
    }

    public void depositar(Dinero monto) {
        validarCuentaOperativa();
        this.saldo = this.saldo.sumar(monto);
    }

    public void retirar(Dinero monto) {
        validarCuentaOperativa();
        this.saldo = this.saldo.restar(monto);
    }

    public void validarCuentaOperativa() {
        if (estado != EstadoCuenta.ACTIVA) {
            throw new IllegalStateException("La cuenta no esta activa");
        }
    }

    public String getId() {
        return id;
    }

    public NumeroCuenta getNumeroCuenta() {
        return numeroCuenta;
    }

    public Dinero getSaldo() {
        return saldo;
    }

    public TipoCuenta getTipoCuenta() {
        return tipoCuenta;
    }

    public String getClienteId() {
        return clienteId;
    }

    public EstadoCuenta getEstado() {
        return estado;
    }

    public void setEstado(EstadoCuenta estado) {
        this.estado = estado;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Cuenta cuenta = (Cuenta) o;
        return Objects.equals(id, cuenta.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
