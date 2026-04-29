package com.bank.application.ports;

import java.util.List;
import java.util.Optional;

import com.bank.domain.entities.Client;

public interface ClientRepositoryPort {

    Client save(Client client);

    Optional<Client> findById(String id);

    Optional<Client> findByEmail(String email);

    Optional<Client> findByIdIdentification(String identificationId);

    List<Client> findAll();
}
