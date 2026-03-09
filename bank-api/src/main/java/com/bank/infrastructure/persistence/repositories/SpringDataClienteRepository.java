package com.bank.infrastructure.persistence.repositories;

import com.bank.infrastructure.persistence.entities.ClienteJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataClienteRepository extends JpaRepository<ClienteJpaEntity, String> {

    Optional<ClienteJpaEntity> findByEmail(String email);
}
