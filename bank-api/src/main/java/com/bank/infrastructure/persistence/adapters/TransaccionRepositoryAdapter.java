package com.bank.infrastructure.persistence.adapters;

import com.bank.application.ports.TransaccionRepositoryPort;
import com.bank.domain.entities.EstadoTransaccion;
import com.bank.domain.entities.Transaccion;
import com.bank.infrastructure.persistence.mappers.TransaccionMapper;
import com.bank.infrastructure.persistence.repositories.SpringDataTransaccionRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class TransaccionRepositoryAdapter implements TransaccionRepositoryPort {

    private final SpringDataTransaccionRepository repository;
    private final TransaccionMapper mapper;

    public TransaccionRepositoryAdapter(SpringDataTransaccionRepository repository, TransaccionMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Transaccion save(Transaccion transaccion) {
        var saved = repository.save(Objects.requireNonNull(mapper.toJpa(transaccion)));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Transaccion> findById(String id) {
        return repository.findById(Objects.requireNonNull(id)).map(mapper::toDomain);
    }

    @Override
    public List<Transaccion> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Transaccion> findByCuentaOrigenInOrCuentaDestinoIn(List<String> cuentasOrigen, List<String> cuentasDestino) {
        return repository.findByCuentaOrigenInOrCuentaDestinoIn(cuentasOrigen, cuentasDestino)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Transaccion> findByEstadoAndFechaBefore(EstadoTransaccion estado, LocalDateTime fecha) {
        return repository.findByEstadoAndFechaBefore(estado, fecha).stream().map(mapper::toDomain).toList();
    }
}
