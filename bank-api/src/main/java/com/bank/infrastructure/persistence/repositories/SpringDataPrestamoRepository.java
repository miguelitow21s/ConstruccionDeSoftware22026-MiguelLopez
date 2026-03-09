package com.bank.infrastructure.persistence.repositories;

import com.bank.infrastructure.persistence.entities.PrestamoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataPrestamoRepository extends JpaRepository<PrestamoJpaEntity, String> {

	List<PrestamoJpaEntity> findByClienteSolicitanteId(String clienteSolicitanteId);
}
