package com.bank.application.ports;

import com.bank.domain.entities.Cliente;

import java.util.Optional;

public interface ClienteRepositoryPort {

    Cliente save(Cliente cliente);

    Optional<Cliente> findById(String id);

    Optional<Cliente> findByEmail(String email);
}
