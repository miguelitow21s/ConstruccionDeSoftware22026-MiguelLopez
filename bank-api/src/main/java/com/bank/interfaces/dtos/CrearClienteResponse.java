package com.bank.interfaces.dtos;

public record CrearClienteResponse(
        String id,
        String idIdentificacion,
        String nombre,
        String email,
        String telefono
) {
}
