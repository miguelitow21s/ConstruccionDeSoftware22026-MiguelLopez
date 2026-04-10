package com.bank.application.services;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class GestionUsuariosOperativosEmpresaService {

    private final Map<String, Map<String, UsuarioOperativoEmpresa>> usuariosPorEmpresa = new ConcurrentHashMap<>();

    public List<UsuarioOperativoEmpresa> listar(String empresaId) {
        return List.copyOf(usuariosPorEmpresa
                .getOrDefault(empresaId, Map.of())
                .values());
    }

    public UsuarioOperativoEmpresa registrar(String empresaId, String username, String nombreCompleto, String email) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username de usuario operativo obligatorio");
        }
        if (nombreCompleto == null || nombreCompleto.isBlank()) {
            throw new IllegalArgumentException("Nombre completo de usuario operativo obligatorio");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email de usuario operativo obligatorio");
        }

        var usuariosEmpresa = usuariosPorEmpresa.computeIfAbsent(empresaId, key -> new ConcurrentHashMap<>());
        if (usuariosEmpresa.containsKey(username)) {
            throw new IllegalArgumentException("El usuario operativo ya existe para la empresa");
        }

        UsuarioOperativoEmpresa usuario = new UsuarioOperativoEmpresa(username, nombreCompleto, email, true);
        usuariosEmpresa.put(username, usuario);
        return usuario;
    }

    public UsuarioOperativoEmpresa cambiarEstado(String empresaId, String username, boolean activo) {
        var usuariosEmpresa = usuariosPorEmpresa.getOrDefault(empresaId, Map.of());
        UsuarioOperativoEmpresa actual = usuariosEmpresa.get(username);
        if (actual == null) {
            throw new IllegalArgumentException("Usuario operativo no encontrado para la empresa");
        }

        UsuarioOperativoEmpresa actualizado = new UsuarioOperativoEmpresa(
                actual.username(),
                actual.nombreCompleto(),
                actual.email(),
                activo
        );

        usuariosPorEmpresa.get(empresaId).put(username, actualizado);
        return actualizado;
    }

    public record UsuarioOperativoEmpresa(String username, String nombreCompleto, String email, boolean activo) {
    }
}
