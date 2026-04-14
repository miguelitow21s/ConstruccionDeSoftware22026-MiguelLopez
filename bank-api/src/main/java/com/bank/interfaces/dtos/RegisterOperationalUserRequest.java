package com.bank.interfaces.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterOperationalUserRequest(
        @NotBlank String username,
        @NotBlank String fullName,
        @NotBlank @Email String email
) {
}
