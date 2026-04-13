package com.bank.infrastructure.persistence.adapters;

import com.bank.application.ports.CuentaRepositoryPort;
import com.bank.domain.entities.Cuenta;
import com.bank.infrastructure.persistence.mappers.CuentaMapper;
import com.bank.infrastructure.persistence.repositories.SpringDataCuentaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class CuentaRepositoryAdapter implements CuentaRepositoryPort {

    private final SpringDataCuentaRepository repository;
    private final CuentaMapper mapper;

    public CuentaRepositoryAdapter(SpringDataCuentaRepository repository, CuentaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Cuenta save(Cuenta cuenta) {
        var saved = repository.save(Objects.requireNonNull(mapper.toJpa(cuenta)));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Cuenta> findById(String id) {
        return repository.findById(Objects.requireNonNull(id)).map(mapper::toDomain);
    }

    @Override
    public Optional<Cuenta> findByNumeroCuenta(String numeroCuenta) {
        return repository.findByNumeroCuenta(numeroCuenta).map(mapper::toDomain);
    }

    @Override
    public List<Cuenta> findByClienteId(String clienteId) {
        return repository.findByClienteId(clienteId).stream().map(mapper::toDomain).toList();
    }
}
