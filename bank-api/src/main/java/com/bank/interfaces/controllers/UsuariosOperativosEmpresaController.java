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

import com.bank.application.usecases.ActualizarEstadoUsuarioOperativoEmpresaUseCase;
import com.bank.application.usecases.ListarUsuariosOperativosEmpresaUseCase;
import com.bank.application.usecases.RegistrarUsuarioOperativoEmpresaUseCase;
import com.bank.interfaces.dtos.RegistrarUsuarioOperativoRequest;
import com.bank.interfaces.dtos.UsuarioOperativoResponse;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/empresas/usuarios-operativos")
@Tag(name = "Usuarios Operativos Empresa", description = "Gestión de usuarios operativos por supervisor de empresa")
@SecurityRequirement(name = "basicAuth")
@PreAuthorize("hasRole('SUPERVISOR_EMPRESA')")
public class UsuariosOperativosEmpresaController {

    private final ListarUsuariosOperativosEmpresaUseCase listarUsuariosUseCase;
    private final RegistrarUsuarioOperativoEmpresaUseCase registrarUsuarioUseCase;
    private final ActualizarEstadoUsuarioOperativoEmpresaUseCase actualizarEstadoUseCase;

    public UsuariosOperativosEmpresaController(ListarUsuariosOperativosEmpresaUseCase listarUsuariosUseCase,
                                               RegistrarUsuarioOperativoEmpresaUseCase registrarUsuarioUseCase,
                                               ActualizarEstadoUsuarioOperativoEmpresaUseCase actualizarEstadoUseCase) {
        this.listarUsuariosUseCase = listarUsuariosUseCase;
        this.registrarUsuarioUseCase = registrarUsuarioUseCase;
        this.actualizarEstadoUseCase = actualizarEstadoUseCase;
    }

    @GetMapping
    public List<UsuarioOperativoResponse> listar() {
        return listarUsuariosUseCase.execute().stream()
                .map(usuario -> new UsuarioOperativoResponse(
                        usuario.username(),
                        usuario.nombreCompleto(),
                        usuario.email(),
                        usuario.activo()
                ))
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioOperativoResponse registrar(@Valid @RequestBody RegistrarUsuarioOperativoRequest request) {
        var usuario = registrarUsuarioUseCase.execute(
                request.username(),
                request.nombreCompleto(),
                request.email()
        );
        return new UsuarioOperativoResponse(usuario.username(), usuario.nombreCompleto(), usuario.email(), usuario.activo());
    }

    @PostMapping("/{username}/activar")
    public UsuarioOperativoResponse activar(@PathVariable String username) {
        var usuario = actualizarEstadoUseCase.activar(username);
        return new UsuarioOperativoResponse(usuario.username(), usuario.nombreCompleto(), usuario.email(), usuario.activo());
    }

    @PostMapping("/{username}/inactivar")
    public UsuarioOperativoResponse inactivar(@PathVariable String username) {
        var usuario = actualizarEstadoUseCase.inactivar(username);
        return new UsuarioOperativoResponse(usuario.username(), usuario.nombreCompleto(), usuario.email(), usuario.activo());
    }
}
