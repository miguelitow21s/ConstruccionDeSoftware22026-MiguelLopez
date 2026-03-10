package com.bank.domain.services;

import com.bank.domain.entities.EstadoUsuario;
import com.bank.domain.entities.UsuarioSistema;

public class ServicioUsuarioSistema {

    public void validarUsuarioActivo(UsuarioSistema usuarioSistema) {
        if (usuarioSistema == null) {
            throw new IllegalArgumentException("Usuario obligatorio");
        }
        if (usuarioSistema.getEstadoUsuario() != EstadoUsuario.ACTIVO) {
            throw new IllegalStateException("El usuario no se encuentra activo");
        }
    }
}
