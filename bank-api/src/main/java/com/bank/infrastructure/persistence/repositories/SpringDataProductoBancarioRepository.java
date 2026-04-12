package com.bank.infrastructure.persistence.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.infrastructure.persistence.entities.ProductoBancarioJpaEntity;

public interface SpringDataProductoBancarioRepository extends JpaRepository<ProductoBancarioJpaEntity, String> {

    Optional<ProductoBancarioJpaEntity> findByCodigoProducto(String codigoProducto);
}
