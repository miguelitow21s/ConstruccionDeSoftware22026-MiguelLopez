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

import com.bank.application.usecases.CreateClientUseCase;
import com.bank.application.usecases.GetClientUseCase;
import com.bank.interfaces.dtos.CreateClientRequest;
import com.bank.interfaces.dtos.CreateClientResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/clients")
@Tag(name = "Clients", description = "Client management")
@SecurityRequirement(name = "basicAuth")
public class ClientController {

    private final CreateClientUseCase createClientUseCase;
    private final GetClientUseCase getClientUseCase;

    public ClientController(CreateClientUseCase createClientUseCase,
                             GetClientUseCase getClientUseCase) {
        this.createClientUseCase = createClientUseCase;
        this.getClientUseCase = getClientUseCase;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ANALYST','SALES')")
    @Operation(summary = "Get client",
               description = "Gets client information for authorized analysis and sales management.")
    public CreateClientResponse get(@PathVariable String id) {
        var client = getClientUseCase.execute(id);
        return new CreateClientResponse(
                client.getId(),
                client.getIdIdentification(),
                client.getName(),
                client.getEmail().value(),
                client.getPhone(),
            client.getBirthDate(),
            client.getAddress(),
                client.getClientType().name(),
                client.getLegalRepresentativeId()
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ANALYST','TELLER','SALES')")
    @Operation(summary = "Create new client",
               description = "Registers a new client in the system. Available for authorized banking staff.")
    public CreateClientResponse create(@Valid @RequestBody CreateClientRequest request) {
        var client = createClientUseCase.execute(
                request.identificationId(),
                request.name(),
                request.email(),
                request.phone(),
                request.birthDate(),
                request.address(),
                request.typeClient(),
                request.legalRepresentativeId()
        );
        return new CreateClientResponse(
                client.getId(),
                client.getIdIdentification(),
                client.getName(),
                client.getEmail().value(),
                client.getPhone(),
                client.getBirthDate(),
                client.getAddress(),
                client.getClientType().name(),
                client.getLegalRepresentativeId()
        );
    }
}
