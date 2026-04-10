package com.bank.application.ports;

import java.util.Optional;

import com.bank.domain.entities.Cliente;

public interface ClienteRepositoryPort {

    Cliente save(Cliente cliente);

    Optional<Cliente> findById(String id);

    Optional<Cliente> findByEmail(String email);

    Optional<Cliente> findByIdIdentificacion(String idIdentificacion);
}
