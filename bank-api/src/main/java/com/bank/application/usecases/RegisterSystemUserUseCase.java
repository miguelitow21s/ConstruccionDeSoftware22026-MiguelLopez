package com.bank.application.usecases;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.bank.application.ports.SystemUserRepositoryPort;
import com.bank.domain.entities.UserStatus;
import com.bank.domain.entities.SystemRole;
import com.bank.domain.entities.SystemUser;
import com.bank.domain.valueobjects.Email;

@Service
public class RegisterSystemUserUseCase {

    private final SystemUserRepositoryPort systemUserRepository;

    public RegisterSystemUserUseCase(SystemUserRepositoryPort systemUserRepository) {
        this.systemUserRepository = systemUserRepository;
    }

    public SystemUser execute(Long userId,
                                  String idRelated,
                                  String fullName,
                                  String identificationId,
                                  String email,
                                  String phone,
                                  LocalDate birthDate,
                                  String address,
                                  SystemRole systemRole,
                                  UserStatus userStatus) {
        systemUserRepository.findByUserId(userId).ifPresent(existing -> {
            throw new IllegalArgumentException("A user with that ID already exists");
        });

        systemUserRepository.findByIdIdentification(identificationId).ifPresent(existing -> {
            throw new IllegalArgumentException("A user with that identification already exists");
        });

        SystemUser systemUser = new SystemUser(
                userId,
                idRelated,
                fullName,
                identificationId,
                new Email(email),
                phone,
                birthDate,
                address,
                systemRole,
                userStatus
        );

        return systemUserRepository.save(systemUser);
    }

    public SystemUser findById(Long userId) {
        return systemUserRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
