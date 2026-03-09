package com.bank.domain.valueobjects;

import java.util.Objects;
import java.util.regex.Pattern;

public final class NumeroCuenta {

    private static final Pattern ACCOUNT_PATTERN = Pattern.compile("^[0-9]{8,20}$");

    private final String value;

    public NumeroCuenta(String value) {
        if (value == null || !ACCOUNT_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Numero de cuenta invalido");
        }
        this.value = value;
    }

    public String value() {
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
        NumeroCuenta that = (NumeroCuenta) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
