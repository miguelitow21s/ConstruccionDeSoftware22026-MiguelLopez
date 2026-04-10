package com.bank.infrastructure.persistence.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.infrastructure.persistence.entities.ClienteJpaEntity;

public interface SpringDataClienteRepository extends JpaRepository<ClienteJpaEntity, String> {

    Optional<ClienteJpaEntity> findByEmail(String email);

    Optional<ClienteJpaEntity> findByIdIdentificacion(String idIdentificacion);
}
