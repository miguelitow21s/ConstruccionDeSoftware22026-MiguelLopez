package com.bank.domain.valueobjects;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class Money {

    private final BigDecimal value;

    public Money(BigDecimal value) {
        if (value == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        this.value = value.setScale(2, RoundingMode.HALF_UP);
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO);
    }

    public static Money positive(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        return new Money(value);
    }

    public Money add(Money other) {
        return new Money(this.value.add(other.value));
    }

    public Money subtract(Money other) {
        BigDecimal result = this.value.subtract(other.value);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Insufficient balance");
        }
        return new Money(result);
    }

    public boolean isLessThan(Money other) {
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
        Money money = (Money) o;
        return Objects.equals(value, money.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
