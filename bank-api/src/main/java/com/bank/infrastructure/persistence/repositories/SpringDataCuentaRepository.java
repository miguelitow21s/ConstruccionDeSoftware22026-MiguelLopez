package com.bank.infrastructure.persistence.repositories;

import com.bank.infrastructure.persistence.entities.CuentaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataCuentaRepository extends JpaRepository<CuentaJpaEntity, String> {

    Optional<CuentaJpaEntity> findByNumeroCuenta(String numeroCuenta);

    List<CuentaJpaEntity> findByClienteId(String clienteId);
}
