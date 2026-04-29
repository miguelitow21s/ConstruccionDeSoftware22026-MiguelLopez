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

import com.bank.application.usecases.RegisterSystemUserUseCase;
import com.bank.application.usecases.UpdateSystemUserStatusUseCase;
import com.bank.domain.entities.SystemUser;
import com.bank.interfaces.dtos.RegisterSystemUserRequest;
import com.bank.interfaces.dtos.SystemUserResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
@Tag(name = "System Users", description = "System user registration and status management")
@SecurityRequirement(name = "basicAuth")
@PreAuthorize("hasRole('ANALYST')")
public class SystemUserController {

    private final RegisterSystemUserUseCase registerSystemUserUseCase;
    private final UpdateSystemUserStatusUseCase updateSystemUserStatusUseCase;

    public SystemUserController(RegisterSystemUserUseCase registerSystemUserUseCase,
                                 UpdateSystemUserStatusUseCase updateSystemUserStatusUseCase) {
        this.registerSystemUserUseCase = registerSystemUserUseCase;
        this.updateSystemUserStatusUseCase = updateSystemUserStatusUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register system user",
               description = "Registers a new system user with a role and initial status. Only analysts can perform this action.")
    public SystemUserResponse register(@Valid @RequestBody RegisterSystemUserRequest request) {
        SystemUser user = registerSystemUserUseCase.execute(
                request.userId(),
                request.idRelated(),
                request.fullName(),
                request.identificationId(),
                request.email(),
                request.phone(),
                request.birthDate(),
                request.address(),
                request.systemRole(),
                request.userStatus()
        );
        return toResponse(user);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get system user by ID",
               description = "Returns a registered system user. Only analysts can access this endpoint.")
    public SystemUserResponse getById(@PathVariable Long userId) {
        SystemUser user = registerSystemUserUseCase.findById(userId);
        return toResponse(user);
    }

    @PostMapping("/{userId}/activate")
    @Operation(summary = "Activate user", description = "Sets user status to ACTIVE.")
    public SystemUserResponse activate(@PathVariable Long userId) {
        return toResponse(updateSystemUserStatusUseCase.activate(userId));
    }

    @PostMapping("/{userId}/deactivate")
    @Operation(summary = "Deactivate user", description = "Sets user status to INACTIVE.")
    public SystemUserResponse deactivate(@PathVariable Long userId) {
        return toResponse(updateSystemUserStatusUseCase.deactivate(userId));
    }

    @PostMapping("/{userId}/block")
    @Operation(summary = "Block user", description = "Sets user status to BLOCKED.")
    public SystemUserResponse block(@PathVariable Long userId) {
        return toResponse(updateSystemUserStatusUseCase.block(userId));
    }

    private SystemUserResponse toResponse(SystemUser user) {
        return new SystemUserResponse(
                user.getUserId(),
                user.getIdRelated(),
                user.getFullName(),
                user.getIdIdentification(),
                user.getEmail().value(),
                user.getPhone(),
                user.getBirthDate(),
                user.getAddress(),
                user.getSystemRole(),
                user.getUserStatus()
        );
    }
}
