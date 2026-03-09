package com.bank.application.ports;

import com.bank.domain.entities.Prestamo;

import java.util.List;
import java.util.Optional;

public interface PrestamoRepositoryPort {

    Prestamo save(Prestamo prestamo);

    Optional<Prestamo> findById(String id);

    List<Prestamo> findAll();
}
