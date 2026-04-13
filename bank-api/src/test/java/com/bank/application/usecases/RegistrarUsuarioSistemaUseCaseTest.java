package com.bank.application.usecases;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.bank.application.ports.UsuarioSistemaRepositoryPort;
import com.bank.domain.entities.EstadoUsuario;
import com.bank.domain.entities.RolSistema;
import com.bank.domain.entities.UsuarioSistema;

class RegistrarUsuarioSistemaUseCaseTest {

    @Test
    void debeRegistrarUsuarioSistemaConDatosValidos() {
        UsuarioSistemaRepositoryPort repository = new InMemoryRepo();
        RegistrarUsuarioSistemaUseCase useCase = new RegistrarUsuarioSistemaUseCase(repository);

        UsuarioSistema usuario = useCase.execute(
                10L,
                "cliente-10",
                "Usuario Sistema",
                "10101010",
                "usuario.sistema@bank.com",
                "3001234567",
                LocalDate.now().minusYears(25),
                "Direccion Principal",
                RolSistema.CLIENTE_PERSONA_NATURAL,
                EstadoUsuario.ACTIVO
        );

        assertEquals(10L, usuario.getIdUsuario());
        assertEquals("10101010", usuario.getIdIdentificacion());
    }

    @Test
    void debeFallarSiIdUsuarioYaExiste() {
        UsuarioSistemaRepositoryPort repository = new InMemoryRepo();
        RegistrarUsuarioSistemaUseCase useCase = new RegistrarUsuarioSistemaUseCase(repository);

        useCase.execute(
                20L,
                "cliente-20",
                "Usuario Uno",
                "20202020",
                "uno@bank.com",
                "3001234567",
                LocalDate.now().minusYears(28),
                "Direccion Uno",
                RolSistema.CLIENTE_PERSONA_NATURAL,
                EstadoUsuario.ACTIVO
        );

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> useCase.execute(
                20L,
                "cliente-21",
                "Usuario Dos",
                "21212121",
                "dos@bank.com",
                "3007654321",
                LocalDate.now().minusYears(30),
                "Direccion Dos",
                RolSistema.CLIENTE_PERSONA_NATURAL,
                EstadoUsuario.ACTIVO
        ));
        assertEquals("Ya existe un usuario con ese ID", thrown.getMessage());
    }

    @Test
    void debeFallarSiIdentificacionYaExisteGlobalmente() {
        UsuarioSistemaRepositoryPort repository = new InMemoryRepo();
        RegistrarUsuarioSistemaUseCase useCase = new RegistrarUsuarioSistemaUseCase(repository);

        useCase.execute(
                30L,
                "cliente-30",
                "Usuario Uno",
                "30303030",
                "uno@bank.com",
                "3001234567",
                LocalDate.now().minusYears(28),
                "Direccion Uno",
                RolSistema.CLIENTE_PERSONA_NATURAL,
                EstadoUsuario.ACTIVO
        );

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> useCase.execute(
                31L,
                "cliente-31",
                "Usuario Dos",
                "30303030",
                "dos@bank.com",
                "3007654321",
                LocalDate.now().minusYears(30),
                "Direccion Dos",
                RolSistema.CLIENTE_PERSONA_NATURAL,
                EstadoUsuario.ACTIVO
        ));
        assertEquals("Ya existe un usuario con esa identificacion", thrown.getMessage());
    }

    private static final class InMemoryRepo extends com.bank.infrastructure.persistence.adapters.InMemoryUsuarioSistemaRepositoryAdapter {
    }
}
