package com.bank.infrastructure.persistence.adapters;

import com.bank.application.ports.PrestamoRepositoryPort;
import com.bank.domain.entities.Prestamo;
import com.bank.infrastructure.persistence.mappers.PrestamoMapper;
import com.bank.infrastructure.persistence.repositories.SpringDataPrestamoRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class PrestamoRepositoryAdapter implements PrestamoRepositoryPort {

    private final SpringDataPrestamoRepository repository;
    private final PrestamoMapper mapper;

    public PrestamoRepositoryAdapter(SpringDataPrestamoRepository repository, PrestamoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @SuppressWarnings("null")
    public Prestamo save(Prestamo prestamo) {
        return mapper.toDomain(
                Objects.requireNonNull(repository.save(mapper.toJpa(prestamo)))
        );
    }

    @Override
    public Optional<Prestamo> findById(String id) {
        return repository.findById(Objects.requireNonNull(id)).map(mapper::toDomain);
    }

    @Override
    public List<Prestamo> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).toList();
    }
}
