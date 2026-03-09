package com.bank.domain.valueobjects;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class Dinero {

    private final BigDecimal value;

    public Dinero(BigDecimal value) {
        if (value == null) {
            throw new IllegalArgumentException("El monto no puede ser nulo");
        }
        this.value = value.setScale(2, RoundingMode.HALF_UP);
    }

    public static Dinero cero() {
        return new Dinero(BigDecimal.ZERO);
    }

    public static Dinero positivo(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero");
        }
        return new Dinero(value);
    }

    public Dinero sumar(Dinero other) {
        return new Dinero(this.value.add(other.value));
    }

    public Dinero restar(Dinero other) {
        BigDecimal result = this.value.subtract(other.value);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Saldo insuficiente");
        }
        return new Dinero(result);
    }

    public boolean esMenorQue(Dinero other) {
        return this.value.compareTo(other.value) < 0;
    }

    public BigDecimal value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Dinero dinero = (Dinero) o;
        return Objects.equals(value, dinero.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
