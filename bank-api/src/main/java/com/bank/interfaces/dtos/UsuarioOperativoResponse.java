package com.bank.interfaces.dtos;

public record UsuarioOperativoResponse(
        String username,
        String nombreCompleto,
        String email,
        boolean activo
) {
}
