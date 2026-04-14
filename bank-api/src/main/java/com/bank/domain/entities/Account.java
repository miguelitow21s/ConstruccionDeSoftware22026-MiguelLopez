package com.bank.domain.entities;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

import com.bank.domain.valueobjects.Money;
import com.bank.domain.valueobjects.AccountNumber;

public class Account {

    private final String id;
    private final AccountNumber accountNumber;
    private Money balance;
    private final AccountType accountType;
    private final String clientId;
    private final String ownerId;
    private final String currency;
    private final LocalDate openingDate;
    private AccountStatus status;

    public Account(AccountNumber accountNumber, Money balance, AccountType accountType, String clientId) {
        this(UUID.randomUUID().toString(), accountNumber, balance, accountType, clientId, clientId, "COP", LocalDate.now(), AccountStatus.ACTIVE);
    }

    public Account(String id, AccountNumber accountNumber, Money balance, AccountType accountType, String clientId, AccountStatus status) {
        this(id, accountNumber, balance, accountType, clientId, clientId, "COP", LocalDate.now(), status);
    }

    public Account(AccountNumber accountNumber,
                  Money balance,
                  AccountType accountType,
                  String clientId,
                  String ownerId,
                  String currency,
                  LocalDate openingDate) {
        this(UUID.randomUUID().toString(), accountNumber, balance, accountType, clientId, ownerId, currency, openingDate, AccountStatus.ACTIVE);
    }

    public Account(String id,
                  AccountNumber accountNumber,
                  Money balance,
                  AccountType accountType,
                  String clientId,
                  String ownerId,
                  String currency,
                  LocalDate openingDate,
                  AccountStatus status) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Invalid account id");
        }
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalArgumentException("Invalid associated client");
        }
        if (ownerId == null || ownerId.isBlank() || ownerId.length() > 20) {
            throw new IllegalArgumentException("Invalid owner id");
        }
        if (currency == null || currency.isBlank() || currency.length() > 5) {
            throw new IllegalArgumentException("Invalid currency");
        }
        if (openingDate == null) {
            throw new IllegalArgumentException("Opening date is required");
        }
        this.id = id;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.accountType = accountType;
        this.clientId = clientId;
        this.ownerId = ownerId;
        this.currency = currency;
        this.openingDate = openingDate;
        this.status = status;
    }

    public void deposit(Money amount) {
        validateOperationalAccount();
        this.balance = this.balance.add(amount);
    }

    public void withdraw(Money amount) {
        validateOperationalAccount();
        this.balance = this.balance.subtract(amount);
    }

    public void validateOperationalAccount() {
        if (status != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Account is not active");
        }
    }

    public String getId() {
        return id;
    }

    public AccountNumber getAccountNumber() {
        return accountNumber;
    }

    public Money getBalance() {
        return balance;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public String getClientId() {
        return clientId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getCurrency() {
        return currency;
    }

    public LocalDate getOpeningDate() {
        return openingDate;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Account account = (Account) o;
        return Objects.equals(id, account.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
