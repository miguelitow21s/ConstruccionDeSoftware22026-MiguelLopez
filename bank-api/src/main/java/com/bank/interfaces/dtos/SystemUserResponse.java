package com.bank.interfaces.dtos;

import java.time.LocalDate;

import com.bank.domain.entities.SystemRole;
import com.bank.domain.entities.UserStatus;

public record SystemUserResponse(
        Long userId,
        String idRelated,
        String fullName,
        String identificationId,
        String email,
        String phone,
        LocalDate birthDate,
        String address,
        SystemRole systemRole,
        UserStatus userStatus
) {
}
