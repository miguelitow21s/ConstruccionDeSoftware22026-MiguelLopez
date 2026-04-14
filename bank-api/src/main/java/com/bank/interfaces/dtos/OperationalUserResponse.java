package com.bank.interfaces.dtos;

public record OperationalUserResponse(
        String username,
        String fullName,
        String email,
        boolean active
) {
}
