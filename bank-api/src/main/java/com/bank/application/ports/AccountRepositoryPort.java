package com.bank.application.ports;

import com.bank.domain.entities.Account;

import java.util.List;
import java.util.Optional;

public interface AccountRepositoryPort {

    Account save(Account account);

    Optional<Account> findById(String id);

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByClientId(String clientId);
}
