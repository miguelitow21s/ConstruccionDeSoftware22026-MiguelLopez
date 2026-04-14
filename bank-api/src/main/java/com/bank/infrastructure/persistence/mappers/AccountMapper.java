package com.bank.infrastructure.persistence.mappers;

import org.springframework.stereotype.Component;

import com.bank.domain.entities.Account;
import com.bank.domain.valueobjects.Money;
import com.bank.domain.valueobjects.AccountNumber;
import com.bank.infrastructure.persistence.entities.AccountJpaEntity;

@Component
public class AccountMapper {

    public AccountJpaEntity toJpa(Account domain) {
        AccountJpaEntity entity = new AccountJpaEntity();
        entity.setId(domain.getId());
        entity.setAccountNumber(domain.getAccountNumber().value());
        entity.setBalance(domain.getBalance().value());
        entity.setAccountType(domain.getAccountType());
        entity.setClientId(domain.getClientId());
        entity.setIdTitular(domain.getOwnerId());
        entity.setMoneda(domain.getCurrency());
        entity.setDateApertura(domain.getOpeningDate());
        entity.setStatus(domain.getStatus());
        return entity;
    }

    public Account toDomain(AccountJpaEntity entity) {
        return new Account(
                entity.getId(),
                new AccountNumber(entity.getAccountNumber()),
                new Money(entity.getBalance()),
                entity.getAccountType(),
                entity.getClientId(),
                entity.getOwnerId(),
                entity.getCurrency(),
                entity.getOpeningDate(),
                entity.getStatus()
        );
    }
}
