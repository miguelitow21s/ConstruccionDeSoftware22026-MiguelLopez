package com.bank.interfaces.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegistrarUsuarioOperativoRequest(
        @NotBlank String username,
        @NotBlank String nombreCompleto,
        @NotBlank @Email String email
) {
}
