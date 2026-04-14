package com.bank.application.ports;

import java.util.List;
import java.util.Optional;

import com.bank.domain.entities.Account;

public interface AccountRepositoryPort {

    Account save(Account account);

    Optional<Account> findById(String id);

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findAll();

    List<Account> findByClientId(String clientId);
}
