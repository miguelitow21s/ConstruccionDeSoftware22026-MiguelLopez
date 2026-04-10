package com.bank.interfaces.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrearClienteRequest(
        @NotBlank @Size(max = 20) String idIdentificacion,
        @NotBlank String nombre,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 7, max = 15) String telefono
) {
}
