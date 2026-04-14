package com.bank.interfaces.dtos;

public record CreateClientResponse(
        String id,
        String identificationId,
        String name,
        String email,
        String phone,
        String typeClient,
        String legalRepresentativeId
) {
}
