package com.bank.interfaces.dtos;

import java.time.LocalDate;

public record CreateClientResponse(
        String id,
        String identificationId,
        String name,
        String email,
        String phone,
        LocalDate birthDate,
        String address,
        String typeClient,
        String legalRepresentativeId
) {
}
