package com.bank.interfaces.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.bank.application.usecases.UpdateCompanyOperationalUserStatusUseCase;
import com.bank.application.usecases.ListCompanyOperationalUsersUseCase;
import com.bank.application.usecases.RegisterCompanyOperationalUserUseCase;
import com.bank.interfaces.dtos.RegisterOperationalUserRequest;
import com.bank.interfaces.dtos.OperationalUserResponse;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/companies/operational-users")
@Tag(name = "Company Operational Users", description = "Operational user management by company supervisor")
@SecurityRequirement(name = "basicAuth")
@PreAuthorize("hasRole('COMPANY_SUPERVISOR')")
public class CompanyOperationalUsersController {

    private final ListCompanyOperationalUsersUseCase listUsersUseCase;
    private final RegisterCompanyOperationalUserUseCase registerUserUseCase;
    private final UpdateCompanyOperationalUserStatusUseCase updateStatusUseCase;

    public CompanyOperationalUsersController(ListCompanyOperationalUsersUseCase listUsersUseCase,
                                               RegisterCompanyOperationalUserUseCase registerUserUseCase,
                                               UpdateCompanyOperationalUserStatusUseCase updateStatusUseCase) {
        this.listUsersUseCase = listUsersUseCase;
        this.registerUserUseCase = registerUserUseCase;
        this.updateStatusUseCase = updateStatusUseCase;
    }

    @GetMapping
    public List<OperationalUserResponse> list() {
        return listUsersUseCase.execute().stream()
                .map(user -> new OperationalUserResponse(
                        user.username(),
                        user.fullName(),
                        user.email(),
                        user.active()
                ))
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OperationalUserResponse register(@Valid @RequestBody RegisterOperationalUserRequest request) {
        var user = registerUserUseCase.execute(
                request.username(),
                request.fullName(),
                request.email()
        );
        return new OperationalUserResponse(user.username(), user.fullName(), user.email(), user.active());
    }

    @PostMapping("/{username}/activate")
    public OperationalUserResponse activate(@PathVariable String username) {
        var user = updateStatusUseCase.activate(username);
        return new OperationalUserResponse(user.username(), user.fullName(), user.email(), user.active());
    }

    @PostMapping("/{username}/deactivate")
    public OperationalUserResponse deactivate(@PathVariable String username) {
        var user = updateStatusUseCase.deactivate(username);
        return new OperationalUserResponse(user.username(), user.fullName(), user.email(), user.active());
    }
}
