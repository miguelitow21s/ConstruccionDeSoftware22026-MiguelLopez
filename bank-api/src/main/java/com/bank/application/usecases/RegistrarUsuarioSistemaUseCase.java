package com.bank.application.usecases;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.bank.application.ports.UsuarioSistemaRepositoryPort;
import com.bank.domain.entities.EstadoUsuario;
import com.bank.domain.entities.RolSistema;
import com.bank.domain.entities.UsuarioSistema;
import com.bank.domain.valueobjects.Email;

@Service
public class RegistrarUsuarioSistemaUseCase {

    private final UsuarioSistemaRepositoryPort usuarioSistemaRepository;

    public RegistrarUsuarioSistemaUseCase(UsuarioSistemaRepositoryPort usuarioSistemaRepository) {
        this.usuarioSistemaRepository = usuarioSistemaRepository;
    }

    public UsuarioSistema execute(Long idUsuario,
                                  String idRelacionado,
                                  String nombreCompleto,
                                  String idIdentificacion,
                                  String correoElectronico,
                                  String telefono,
                                  LocalDate fechaNacimiento,
                                  String direccion,
                                  RolSistema rolSistema,
                                  EstadoUsuario estadoUsuario) {
        usuarioSistemaRepository.findByIdUsuario(idUsuario).ifPresent(existing -> {
            throw new IllegalArgumentException("Ya existe un usuario con ese ID");
        });

        usuarioSistemaRepository.findByIdIdentificacion(idIdentificacion).ifPresent(existing -> {
            throw new IllegalArgumentException("Ya existe un usuario con esa identificacion");
        });

        UsuarioSistema usuarioSistema = new UsuarioSistema(
                idUsuario,
                idRelacionado,
                nombreCompleto,
                idIdentificacion,
                new Email(correoElectronico),
                telefono,
                fechaNacimiento,
                direccion,
                rolSistema,
                estadoUsuario
        );

        return usuarioSistemaRepository.save(usuarioSistema);
    }
}
