package com.bank.infrastructure.persistence.adapters;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.bank.application.ports.BankingProductRepositoryPort;
import com.bank.domain.entities.BankingProduct;
import com.bank.infrastructure.persistence.mappers.BankingProductMapper;
import com.bank.infrastructure.persistence.repositories.SpringDataBankingProductRepository;

@Component
public class BankingProductRepositoryAdapter implements BankingProductRepositoryPort {

    private final SpringDataBankingProductRepository repository;
    private final BankingProductMapper mapper;

    public BankingProductRepositoryAdapter(SpringDataBankingProductRepository repository,
                                             BankingProductMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public BankingProduct save(BankingProduct bankingProduct) {
        var saved = repository.save(Objects.requireNonNull(mapper.toJpa(bankingProduct)));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<BankingProduct> findByProductCode(String productCode) {
        return repository.findByProductCode(productCode).map(mapper::toDomain);
    }

    @Override
    public List<BankingProduct> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).toList();
    }
}
