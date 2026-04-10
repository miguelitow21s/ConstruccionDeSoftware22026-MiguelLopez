package com.bank.interfaces.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.bank.application.usecases.ConsultarClienteUseCase;
import com.bank.application.usecases.CrearClienteUseCase;
import com.bank.interfaces.dtos.CrearClienteRequest;
import com.bank.interfaces.dtos.CrearClienteResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/clientes")
@Tag(name = "Clientes", description = "Gestión de clientes del banco")
@SecurityRequirement(name = "basicAuth")
public class ClienteController {

    private final CrearClienteUseCase crearClienteUseCase;
    private final ConsultarClienteUseCase consultarClienteUseCase;

    public ClienteController(CrearClienteUseCase crearClienteUseCase,
                             ConsultarClienteUseCase consultarClienteUseCase) {
        this.crearClienteUseCase = crearClienteUseCase;
        this.consultarClienteUseCase = consultarClienteUseCase;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ANALISTA','COMERCIAL')")
    @Operation(summary = "Consultar cliente",
               description = "Permite consultar informacion de cliente para analisis y gestion comercial autorizada.")
    public CrearClienteResponse consultar(@PathVariable String id) {
        var cliente = consultarClienteUseCase.execute(id);
        return new CrearClienteResponse(
                cliente.getId(),
                cliente.getIdIdentificacion(),
                cliente.getNombre(),
                cliente.getEmail().value(),
                cliente.getTelefono(),
                cliente.getTipoCliente().name(),
                cliente.getRepresentanteLegalId()
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ANALISTA','VENTANILLA','COMERCIAL')")
    @Operation(summary = "Crear nuevo cliente", 
               description = "Registra un nuevo cliente en el sistema. Disponible para personal bancario autorizado.")
    public CrearClienteResponse crear(@Valid @RequestBody CrearClienteRequest request) {
        var cliente = crearClienteUseCase.execute(
                request.idIdentificacion(),
                request.nombre(),
                request.email(),
                request.telefono(),
                request.tipoCliente(),
                request.representanteLegalId()
        );
        return new CrearClienteResponse(
                cliente.getId(),
                cliente.getIdIdentificacion(),
                cliente.getNombre(),
                cliente.getEmail().value(),
                cliente.getTelefono(),
                cliente.getTipoCliente().name(),
                cliente.getRepresentanteLegalId()
        );
    }
}
