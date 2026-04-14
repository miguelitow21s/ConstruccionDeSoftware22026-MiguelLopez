package com.bank.application.ports;

import java.util.Optional;

import com.bank.domain.entities.SystemUser;

public interface SystemUserRepositoryPort {

    SystemUser save(SystemUser systemUser);

    Optional<SystemUser> findByUserId(Long userId);

    Optional<SystemUser> findByIdIdentification(String identificationId);
}
