package com.bank.infrastructure.persistence.adapters;

import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.bank.application.ports.ClientRepositoryPort;
import com.bank.domain.entities.Client;
import com.bank.infrastructure.persistence.mappers.ClientMapper;
import com.bank.infrastructure.persistence.repositories.SpringDataClientRepository;

@Component
public class ClientRepositoryAdapter implements ClientRepositoryPort {

    private final SpringDataClientRepository repository;
    private final ClientMapper mapper;

    public ClientRepositoryAdapter(SpringDataClientRepository repository, ClientMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Client save(Client client) {
        var saved = repository.save(Objects.requireNonNull(mapper.toJpa(client)));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Client> findById(String id) {
        return repository.findById(Objects.requireNonNull(id)).map(mapper::toDomain);
    }

    @Override
    public Optional<Client> findByEmail(String email) {
        return repository.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public Optional<Client> findByIdIdentification(String identificationId) {
        return repository.findByIdIdentification(identificationId).map(mapper::toDomain);
    }
}
