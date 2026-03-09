package com.bank.interfaces.controllers;

import com.bank.application.usecases.CrearClienteUseCase;
import com.bank.interfaces.dtos.CrearClienteRequest;
import com.bank.interfaces.dtos.CrearClienteResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final CrearClienteUseCase crearClienteUseCase;

    public ClienteController(CrearClienteUseCase crearClienteUseCase) {
        this.crearClienteUseCase = crearClienteUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ANALISTA','VENTANILLA','COMERCIAL')")
    public CrearClienteResponse crear(@Valid @RequestBody CrearClienteRequest request) {
        var cliente = crearClienteUseCase.execute(request.nombre(), request.email(), request.telefono());
        return new CrearClienteResponse(
                cliente.getId(),
                cliente.getNombre(),
                cliente.getEmail().value(),
                cliente.getTelefono()
        );
    }
}
