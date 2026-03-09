package com.bank.interfaces.dtos;

public record CrearClienteResponse(
        String id,
        String nombre,
        String email,
        String telefono
) {
}
