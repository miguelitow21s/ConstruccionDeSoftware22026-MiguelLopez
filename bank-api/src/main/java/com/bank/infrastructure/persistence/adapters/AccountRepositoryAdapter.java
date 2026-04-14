package com.bank.infrastructure.persistence.adapters;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.bank.application.ports.AccountRepositoryPort;
import com.bank.domain.entities.Account;
import com.bank.infrastructure.persistence.mappers.AccountMapper;
import com.bank.infrastructure.persistence.repositories.SpringDataAccountRepository;

@Component
public class AccountRepositoryAdapter implements AccountRepositoryPort {

    private final SpringDataAccountRepository repository;
    private final AccountMapper mapper;

    public AccountRepositoryAdapter(SpringDataAccountRepository repository, AccountMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Account save(Account account) {
        var saved = repository.save(Objects.requireNonNull(mapper.toJpa(account)));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Account> findById(String id) {
        return repository.findById(Objects.requireNonNull(id)).map(mapper::toDomain);
    }

    @Override
    public Optional<Account> findByAccountNumber(String accountNumber) {
        return repository.findByAccountNumber(accountNumber).map(mapper::toDomain);
    }

    @Override
    public List<Account> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Account> findByClientId(String clientId) {
        return repository.findByClientId(clientId).stream().map(mapper::toDomain).toList();
    }
}
