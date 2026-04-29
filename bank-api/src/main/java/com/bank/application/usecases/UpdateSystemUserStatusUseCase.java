package com.bank.application.usecases;

import org.springframework.stereotype.Service;

import com.bank.application.ports.SystemUserRepositoryPort;
import com.bank.domain.entities.SystemUser;

@Service
public class UpdateSystemUserStatusUseCase {

    private final SystemUserRepositoryPort systemUserRepository;

    public UpdateSystemUserStatusUseCase(SystemUserRepositoryPort systemUserRepository) {
        this.systemUserRepository = systemUserRepository;
    }

    public SystemUser activate(Long userId) {
        SystemUser user = findOrThrow(userId);
        user.activate();
        return systemUserRepository.save(user);
    }

    public SystemUser deactivate(Long userId) {
        SystemUser user = findOrThrow(userId);
        user.deactivate();
        return systemUserRepository.save(user);
    }

    public SystemUser block(Long userId) {
        SystemUser user = findOrThrow(userId);
        user.block();
        return systemUserRepository.save(user);
    }

    private SystemUser findOrThrow(Long userId) {
        return systemUserRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
