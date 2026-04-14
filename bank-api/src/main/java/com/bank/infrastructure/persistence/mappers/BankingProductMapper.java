package com.bank.infrastructure.persistence.mappers;

import org.springframework.stereotype.Component;

import com.bank.domain.entities.BankingProduct;
import com.bank.infrastructure.persistence.entities.BankingProductJpaEntity;

@Component
public class BankingProductMapper {

    public BankingProductJpaEntity toJpa(BankingProduct domain) {
        BankingProductJpaEntity entity = new BankingProductJpaEntity();
        entity.setProductCode(domain.getProductCode());
        entity.setNameProducto(domain.getNameProducto());
        entity.setCategoria(domain.getCategoria());
        entity.setRequiereApproval(domain.isRequiereApproval());
        return entity;
    }

    public BankingProduct toDomain(BankingProductJpaEntity entity) {
        return new BankingProduct(
                entity.getProductCode(),
                entity.getNameProducto(),
                entity.getCategoria(),
                entity.isRequiereApproval()
        );
    }
}
