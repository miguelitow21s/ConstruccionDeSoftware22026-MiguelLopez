package com.bank.infrastructure.persistence.repositories;

import com.bank.infrastructure.persistence.entities.PrestamoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataPrestamoRepository extends JpaRepository<PrestamoJpaEntity, String> {
}
