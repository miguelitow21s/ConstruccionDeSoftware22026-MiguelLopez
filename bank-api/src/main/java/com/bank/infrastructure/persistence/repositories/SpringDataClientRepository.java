package com.bank.infrastructure.persistence.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.infrastructure.persistence.entities.ClientJpaEntity;

public interface SpringDataClientRepository extends JpaRepository<ClientJpaEntity, String> {

    Optional<ClientJpaEntity> findByEmail(String email);

    Optional<ClientJpaEntity> findByIdIdentification(String identificationId);
}
