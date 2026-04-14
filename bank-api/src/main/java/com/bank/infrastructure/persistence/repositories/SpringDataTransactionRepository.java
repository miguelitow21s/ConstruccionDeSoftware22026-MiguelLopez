package com.bank.infrastructure.persistence.repositories;

import com.bank.domain.entities.TransactionStatus;
import com.bank.infrastructure.persistence.entities.TransactionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SpringDataTransactionRepository extends JpaRepository<TransactionJpaEntity, String> {

    List<TransactionJpaEntity> findByStatusAndDateBefore(TransactionStatus status, LocalDateTime date);

    List<TransactionJpaEntity> findByAccountSourceInOrAccountDestinationIn(List<String> accountsSource, List<String> accountsDestination);
}
