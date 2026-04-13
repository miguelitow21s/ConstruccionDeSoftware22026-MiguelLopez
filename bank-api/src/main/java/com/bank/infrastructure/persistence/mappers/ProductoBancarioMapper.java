package com.bank.infrastructure.persistence.mappers;

import org.springframework.stereotype.Component;

import com.bank.domain.entities.ProductoBancario;
import com.bank.infrastructure.persistence.entities.ProductoBancarioJpaEntity;

@Component
public class ProductoBancarioMapper {

    public ProductoBancarioJpaEntity toJpa(ProductoBancario domain) {
        ProductoBancarioJpaEntity entity = new ProductoBancarioJpaEntity();
        entity.setCodigoProducto(domain.getCodigoProducto());
        entity.setNombreProducto(domain.getNombreProducto());
        entity.setCategoria(domain.getCategoria());
        entity.setRequiereAprobacion(domain.isRequiereAprobacion());
        return entity;
    }

    public ProductoBancario toDomain(ProductoBancarioJpaEntity entity) {
        return new ProductoBancario(
                entity.getCodigoProducto(),
                entity.getNombreProducto(),
                entity.getCategoria(),
                entity.isRequiereAprobacion()
        );
    }
}
