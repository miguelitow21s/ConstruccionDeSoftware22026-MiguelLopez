package com.bank.infrastructure.persistence.adapters;

import com.bank.application.ports.TransactionRepositoryPort;
import com.bank.domain.entities.TransactionStatus;
import com.bank.domain.entities.Transaction;
import com.bank.infrastructure.persistence.mappers.TransactionMapper;
import com.bank.infrastructure.persistence.repositories.SpringDataTransactionRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class TransactionRepositoryAdapter implements TransactionRepositoryPort {

    private final SpringDataTransactionRepository repository;
    private final TransactionMapper mapper;

    public TransactionRepositoryAdapter(SpringDataTransactionRepository repository, TransactionMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Transaction save(Transaction transaction) {
        var saved = repository.save(Objects.requireNonNull(mapper.toJpa(transaction)));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Transaction> findById(String id) {
        return repository.findById(Objects.requireNonNull(id)).map(mapper::toDomain);
    }

    @Override
    public List<Transaction> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Transaction> findByAccountSourceInOrAccountDestinationIn(List<String> accountsSource, List<String> accountsDestination) {
        return repository.findByAccountSourceInOrAccountDestinationIn(accountsSource, accountsDestination)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Transaction> findByStatusAndDateBefore(TransactionStatus status, LocalDateTime date) {
        return repository.findByStatusAndDateBefore(status, date).stream().map(mapper::toDomain).toList();
    }
}
