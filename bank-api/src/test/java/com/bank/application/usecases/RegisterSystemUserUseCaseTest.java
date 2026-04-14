package com.bank.application.usecases;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.bank.application.ports.SystemUserRepositoryPort;
import com.bank.domain.entities.SystemRole;
import com.bank.domain.entities.SystemUser;
import com.bank.domain.entities.UserStatus;

class RegisterSystemUserUseCaseTest {

    @Test
    void debeRegisterSystemUserConDatosValidos() {
        SystemUserRepositoryPort repository = new InMemoryRepo();
        RegisterSystemUserUseCase useCase = new RegisterSystemUserUseCase(repository);

        SystemUser user = useCase.execute(
                10L,
                "client-10",
                "User System",
                "10101010",
                "user.system@bank.com",
                "3001234567",
                LocalDate.now().minusYears(25),
                "Address Principal",
                SystemRole.NATURAL_PERSON_CLIENT,
                UserStatus.ACTIVE
        );

        assertEquals(10L, user.getUserId());
        assertEquals("10101010", user.getIdIdentification());
    }

    @Test
    void debeFallarSiIdUserYaExiste() {
        SystemUserRepositoryPort repository = new InMemoryRepo();
        RegisterSystemUserUseCase useCase = new RegisterSystemUserUseCase(repository);

        useCase.execute(
                20L,
                "client-20",
                "User Uno",
                "20202020",
                "uno@bank.com",
                "3001234567",
                LocalDate.now().minusYears(28),
                "Address Uno",
                SystemRole.NATURAL_PERSON_CLIENT,
                UserStatus.ACTIVE
        );

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> useCase.execute(
                20L,
                "client-21",
                "User Dos",
                "21212121",
                "dos@bank.com",
                "3007654321",
                LocalDate.now().minusYears(30),
                "Address Dos",
                SystemRole.NATURAL_PERSON_CLIENT,
                UserStatus.ACTIVE
        ));
        assertEquals("A user with that ID already exists", thrown.getMessage());
    }

    @Test
    void debeFallarSiIdentificationYaExisteGlobalmente() {
        SystemUserRepositoryPort repository = new InMemoryRepo();
        RegisterSystemUserUseCase useCase = new RegisterSystemUserUseCase(repository);

        useCase.execute(
                30L,
                "client-30",
                "User Uno",
                "30303030",
                "uno@bank.com",
                "3001234567",
                LocalDate.now().minusYears(28),
                "Address Uno",
                SystemRole.NATURAL_PERSON_CLIENT,
                UserStatus.ACTIVE
        );

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> useCase.execute(
                31L,
                "client-31",
                "User Dos",
                "30303030",
                "dos@bank.com",
                "3007654321",
                LocalDate.now().minusYears(30),
                "Address Dos",
                SystemRole.NATURAL_PERSON_CLIENT,
                UserStatus.ACTIVE
        ));
        assertEquals("A user with that identification already exists", thrown.getMessage());
    }

    private static final class InMemoryRepo extends com.bank.infrastructure.persistence.adapters.InMemorySystemUserRepositoryAdapter {
    }
}
