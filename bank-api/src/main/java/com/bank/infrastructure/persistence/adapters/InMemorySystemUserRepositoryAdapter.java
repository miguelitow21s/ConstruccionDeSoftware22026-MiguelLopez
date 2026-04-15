package com.bank.infrastructure.persistence.adapters;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.bank.application.ports.SystemUserRepositoryPort;
import com.bank.domain.entities.SystemUser;

@Component
public class InMemorySystemUserRepositoryAdapter implements SystemUserRepositoryPort {

    private final Map<Long, SystemUser> usersById = new ConcurrentHashMap<>();
    private final Map<String, Long> userIdByIdentification = new ConcurrentHashMap<>();

    @Override
    public synchronized SystemUser save(SystemUser systemUser) {
        Long userId = systemUser.getUserId();
        String identificationId = systemUser.getIdIdentification();

        Long existingIdByIdentification = userIdByIdentification.get(identificationId);
        if (existingIdByIdentification != null && !existingIdByIdentification.equals(userId)) {
            throw new IllegalArgumentException("A user with that identification already exists");
        }

        usersById.put(userId, systemUser);
        userIdByIdentification.put(identificationId, userId);
        return systemUser;
    }

    @Override
    public Optional<SystemUser> findByUserId(Long userId) {
        return Optional.ofNullable(usersById.get(userId));
    }

    @Override
    public Optional<SystemUser> findByIdIdentification(String identificationId) {
        Long userId = userIdByIdentification.get(identificationId);
        if (userId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(usersById.get(userId));
    }
}
