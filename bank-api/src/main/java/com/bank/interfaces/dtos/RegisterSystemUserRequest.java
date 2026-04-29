package com.bank.interfaces.dtos;

import java.time.LocalDate;

import com.bank.domain.entities.SystemRole;
import com.bank.domain.entities.UserStatus;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RegisterSystemUserRequest(
        @NotNull @Positive Long userId,
        String idRelated,
        @NotBlank String fullName,
        @NotBlank String identificationId,
        @NotBlank @Email String email,
        @NotBlank String phone,
        LocalDate birthDate,
        @NotBlank String address,
        @NotNull SystemRole systemRole,
        @NotNull UserStatus userStatus
) {
}
