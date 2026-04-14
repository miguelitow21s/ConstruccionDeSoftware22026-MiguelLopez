package com.bank.infrastructure.persistence.mappers;

import org.springframework.stereotype.Component;

import com.bank.domain.entities.BankingProduct;
import com.bank.infrastructure.persistence.entities.BankingProductJpaEntity;

@Component
public class BankingProductMapper {

    public BankingProductJpaEntity toJpa(BankingProduct domain) {
        BankingProductJpaEntity entity = new BankingProductJpaEntity();
        entity.setProductCode(domain.getProductCode());
        entity.setProductName(domain.getProductName());
        entity.setCategory(domain.getCategory());
        entity.setRequiresApproval(domain.isRequiresApproval());
        return entity;
    }

    public BankingProduct toDomain(BankingProductJpaEntity entity) {
        return new BankingProduct(
                entity.getProductCode(),
            entity.getProductName(),
            entity.getCategory(),
            entity.isRequiresApproval()
        );
    }
}
