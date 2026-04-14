package com.bank.infrastructure.persistence.repositories;

import com.bank.infrastructure.persistence.entities.AccountJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataAccountRepository extends JpaRepository<AccountJpaEntity, String> {

    Optional<AccountJpaEntity> findByAccountNumber(String accountNumber);

    List<AccountJpaEntity> findByClientId(String clientId);
}
