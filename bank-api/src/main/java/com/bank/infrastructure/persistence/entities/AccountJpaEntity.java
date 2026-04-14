package com.bank.infrastructure.persistence.entities;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.bank.domain.entities.AccountStatus;
import com.bank.domain.entities.AccountType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "accounts")
public class AccountJpaEntity {

    @Id
    private String id;

    @Column(nullable = false, unique = true, length = 20)
    private String accountNumber;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType accountType;

    @Column(nullable = false)
    private String clientId;

    @Column(nullable = false, length = 20)
    private String ownerId;

    @Column(nullable = false, length = 5)
    private String currency;

    @Column(nullable = false)
    private LocalDate openingDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setIdTitular(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setMoneda(String currency) {
        this.currency = currency;
    }

    public LocalDate getOpeningDate() {
        return openingDate;
    }

    public void setDateApertura(LocalDate openingDate) {
        this.openingDate = openingDate;
    }
}
