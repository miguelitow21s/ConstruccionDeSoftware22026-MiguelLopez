package com.bank.infrastructure.persistence.adapters;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.bank.application.ports.ProductoBancarioRepositoryPort;
import com.bank.domain.entities.ProductoBancario;
import com.bank.infrastructure.persistence.mappers.ProductoBancarioMapper;
import com.bank.infrastructure.persistence.repositories.SpringDataProductoBancarioRepository;

@Component
public class ProductoBancarioRepositoryAdapter implements ProductoBancarioRepositoryPort {

    private final SpringDataProductoBancarioRepository repository;
    private final ProductoBancarioMapper mapper;

    public ProductoBancarioRepositoryAdapter(SpringDataProductoBancarioRepository repository,
                                             ProductoBancarioMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @SuppressWarnings("null")
    public ProductoBancario save(ProductoBancario productoBancario) {
        var saved = repository.save(Objects.requireNonNull(mapper.toJpa(productoBancario)));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<ProductoBancario> findByCodigoProducto(String codigoProducto) {
        return repository.findByCodigoProducto(codigoProducto).map(mapper::toDomain);
    }

    @Override
    public List<ProductoBancario> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).toList();
    }
}
