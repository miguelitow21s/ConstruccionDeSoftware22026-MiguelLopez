package com.bank.infrastructure.persistence.adapters;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.bank.application.ports.UsuarioSistemaRepositoryPort;
import com.bank.domain.entities.UsuarioSistema;

@Component
public class InMemoryUsuarioSistemaRepositoryAdapter implements UsuarioSistemaRepositoryPort {

    private final Map<Long, UsuarioSistema> usuariosPorId = new ConcurrentHashMap<>();
    private final Map<String, Long> idUsuarioPorIdentificacion = new ConcurrentHashMap<>();

    @Override
    public synchronized UsuarioSistema save(UsuarioSistema usuarioSistema) {
        Long idUsuario = usuarioSistema.getIdUsuario();
        String idIdentificacion = usuarioSistema.getIdIdentificacion();

        Long idExistentePorIdentificacion = idUsuarioPorIdentificacion.get(idIdentificacion);
        if (idExistentePorIdentificacion != null && !idExistentePorIdentificacion.equals(idUsuario)) {
            throw new IllegalArgumentException("Ya existe un usuario con esa identificacion");
        }

        usuariosPorId.put(idUsuario, usuarioSistema);
        idUsuarioPorIdentificacion.put(idIdentificacion, idUsuario);
        return usuarioSistema;
    }

    @Override
    public Optional<UsuarioSistema> findByIdUsuario(Long idUsuario) {
        return Optional.ofNullable(usuariosPorId.get(idUsuario));
    }

    @Override
    public Optional<UsuarioSistema> findByIdIdentificacion(String idIdentificacion) {
        Long idUsuario = idUsuarioPorIdentificacion.get(idIdentificacion);
        if (idUsuario == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(usuariosPorId.get(idUsuario));
    }
}
