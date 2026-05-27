package com.bank.infrastructure.persistence.mappers;

import org.springframework.stereotype.Component;

import com.bank.domain.entities.Transaction;
import com.bank.domain.valueobjects.Money;
import com.bank.infrastructure.persistence.entities.TransactionJpaEntity;

// Mapper de transacciones. El campo typeTransaction en la entidad JPA es el nombre en base de datos,
// mientras que en el dominio se llama igual pero el getter se llama getTransactionType().
// El mapper es el puente que absorbe esas diferencias de nombre.
@Component
public class TransactionMapper {

    public TransactionJpaEntity toJpa(Transaction domain) {
        TransactionJpaEntity entity = new TransactionJpaEntity();
        entity.setId(domain.getId());
        entity.setTransactionType(domain.getTransactionType());
        entity.setAmount(domain.getAmount().value());
        entity.setDate(domain.getDate());
        entity.setApprovalDate(domain.getApprovalDate());
        entity.setSourceAccount(domain.getSourceAccount());
        entity.setDestinationAccount(domain.getDestinationAccount());
        entity.setCreatorUserId(domain.getCreatorUserId());
        entity.setApproverUserId(domain.getApproverUserId());
        entity.setStatus(domain.getStatus());
        return entity;
    }

    public Transaction toDomain(TransactionJpaEntity entity) {
        return new Transaction(
                entity.getId(),
                entity.getTransactionType(),
                new Money(entity.getAmount()),
                entity.getDate(),
                entity.getApprovalDate(),
                entity.getSourceAccount(),
                entity.getDestinationAccount(),
                entity.getStatus(),
                entity.getCreatorUserId(),
                entity.getApproverUserId()
        );
    }
}
