package com.bank.application.ports;

import com.bank.domain.entities.TransactionStatus;
import com.bank.domain.entities.Transaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepositoryPort {

    Transaction save(Transaction transaction);

    Optional<Transaction> findById(String id);

    List<Transaction> findAll();

    List<Transaction> findByAccountSourceInOrAccountDestinationIn(List<String> accountsSource, List<String> accountsDestination);

    List<Transaction> findByStatusAndDateBefore(TransactionStatus status, LocalDateTime date);
}
