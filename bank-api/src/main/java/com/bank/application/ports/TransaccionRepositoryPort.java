package com.bank.application.ports;

import com.bank.domain.entities.EstadoTransaccion;
import com.bank.domain.entities.Transaccion;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransaccionRepositoryPort {

    Transaccion save(Transaccion transaccion);

    Optional<Transaccion> findById(String id);

    List<Transaccion> findAll();

    List<Transaccion> findByEstadoAndFechaBefore(EstadoTransaccion estado, LocalDateTime fecha);
}
