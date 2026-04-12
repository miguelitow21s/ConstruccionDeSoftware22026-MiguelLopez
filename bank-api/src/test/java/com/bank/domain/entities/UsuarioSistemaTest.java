package com.bank.domain.entities;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.bank.domain.valueobjects.Email;

class UsuarioSistemaTest {

    @Test
    void debeCrearUsuarioConDatosValidos() {
        UsuarioSistema usuario = new UsuarioSistema(
                1L,
                "cliente-1",
                "Usuario Valido",
                "1234567890",
                new Email("usuario@bank.com"),
                "3001234567",
                LocalDate.now().minusYears(30),
                "Direccion 123",
                RolSistema.CLIENTE_PERSONA_NATURAL,
                EstadoUsuario.ACTIVO
        );

        assertEquals(1L, usuario.getIdUsuario());
        assertEquals("1234567890", usuario.getIdIdentificacion());
        assertEquals(EstadoUsuario.ACTIVO, usuario.getEstadoUsuario());
    }

    @Test
    void debeFallarSiIdUsuarioNoEsPositivo() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> new UsuarioSistema(
                0L,
                "cliente-1",
                "Usuario Invalido",
                "1234567890",
                new Email("usuario@bank.com"),
                "3001234567",
                LocalDate.now().minusYears(30),
                "Direccion 123",
                RolSistema.CLIENTE_PERSONA_NATURAL,
                EstadoUsuario.ACTIVO
        ));
        assertEquals("ID de usuario invalido", thrown.getMessage());
    }

    @Test
    void debeFallarSiIdentificacionSuperaLongitudMaxima() {
        String identificacionLarga = "123456789012345678901";

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> new UsuarioSistema(
                2L,
                "cliente-1",
                "Usuario Invalido",
                identificacionLarga,
                new Email("usuario@bank.com"),
                "3001234567",
                LocalDate.now().minusYears(30),
                "Direccion 123",
                RolSistema.CLIENTE_PERSONA_NATURAL,
                EstadoUsuario.ACTIVO
        ));
        assertEquals("Identificacion obligatoria", thrown.getMessage());
    }

    @Test
    void debeFallarSiTelefonoNoEsNumerico() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> new UsuarioSistema(
                3L,
                "cliente-1",
                "Usuario Invalido",
                "1234567890",
                new Email("usuario@bank.com"),
                "30012ABC",
                LocalDate.now().minusYears(30),
                "Direccion 123",
                RolSistema.CLIENTE_PERSONA_NATURAL,
                EstadoUsuario.ACTIVO
        ));
        assertEquals("Telefono invalido", thrown.getMessage());
    }

    @Test
    void debeFallarSiPersonaNaturalEsMenorDeEdad() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> new UsuarioSistema(
                4L,
                "cliente-1",
                "Usuario Menor",
                "1234567890",
                new Email("usuario@bank.com"),
                "3001234567",
                LocalDate.now().minusYears(17),
                "Direccion 123",
                RolSistema.CLIENTE_PERSONA_NATURAL,
                EstadoUsuario.ACTIVO
        ));
        assertEquals("El usuario debe ser mayor de edad", thrown.getMessage());
    }

    @Test
    void debeFallarSiIdRelacionadoEsObligatorioPorRol() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> new UsuarioSistema(
                5L,
                null,
                "Supervisor",
                "1234567890",
                new Email("supervisor@bank.com"),
                "3001234567",
                null,
                "Direccion 123",
                RolSistema.SUPERVISOR_EMPRESA,
                EstadoUsuario.ACTIVO
        ));
        assertEquals("ID relacionado obligatorio para el rol SUPERVISOR_EMPRESA", thrown.getMessage());
    }
}
