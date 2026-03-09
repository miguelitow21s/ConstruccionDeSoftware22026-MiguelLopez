package com.bank.infrastructure.persistence.adapters;

import com.bank.application.ports.ClienteRepositoryPort;
import com.bank.domain.entities.Cliente;
import com.bank.infrastructure.persistence.mappers.ClienteMapper;
import com.bank.infrastructure.persistence.repositories.SpringDataClienteRepository;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
public class ClienteRepositoryAdapter implements ClienteRepositoryPort {

    private final SpringDataClienteRepository repository;
    private final ClienteMapper mapper;

    public ClienteRepositoryAdapter(SpringDataClienteRepository repository, ClienteMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @SuppressWarnings("null")
    public Cliente save(Cliente cliente) {
        var saved = repository.save(Objects.requireNonNull(mapper.toJpa(cliente)));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Cliente> findById(String id) {
        return repository.findById(Objects.requireNonNull(id)).map(mapper::toDomain);
    }

    @Override
    public Optional<Cliente> findByEmail(String email) {
        return repository.findByEmail(email).map(mapper::toDomain);
    }
}
