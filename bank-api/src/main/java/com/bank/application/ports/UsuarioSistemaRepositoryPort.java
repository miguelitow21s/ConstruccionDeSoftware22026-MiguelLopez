package com.bank.application.ports;

import java.util.Optional;

import com.bank.domain.entities.UsuarioSistema;

public interface UsuarioSistemaRepositoryPort {

    UsuarioSistema save(UsuarioSistema usuarioSistema);

    Optional<UsuarioSistema> findByIdUsuario(Long idUsuario);

    Optional<UsuarioSistema> findByIdIdentificacion(String idIdentificacion);
}
