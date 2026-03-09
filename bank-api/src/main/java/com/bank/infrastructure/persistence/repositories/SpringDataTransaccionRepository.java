package com.bank.infrastructure.persistence.repositories;

import com.bank.domain.entities.EstadoTransaccion;
import com.bank.infrastructure.persistence.entities.TransaccionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SpringDataTransaccionRepository extends JpaRepository<TransaccionJpaEntity, String> {

    List<TransaccionJpaEntity> findByEstadoAndFechaBefore(EstadoTransaccion estado, LocalDateTime fecha);
}
