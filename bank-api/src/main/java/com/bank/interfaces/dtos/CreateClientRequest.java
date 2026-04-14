package com.bank.interfaces.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateClientRequest(
        @NotBlank @Size(max = 20) String identificationId,
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 7, max = 15) String phone,
        String typeClient,
        String legalRepresentativeId
) {
}
