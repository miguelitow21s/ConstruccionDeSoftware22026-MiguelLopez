package com.bank.infrastructure.persistence.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.infrastructure.persistence.entities.BankingProductJpaEntity;

public interface SpringDataBankingProductRepository extends JpaRepository<BankingProductJpaEntity, String> {

    Optional<BankingProductJpaEntity> findByProductCode(String productCode);
}
