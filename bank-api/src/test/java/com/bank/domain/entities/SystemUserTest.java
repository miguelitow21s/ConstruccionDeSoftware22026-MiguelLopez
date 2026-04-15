package com.bank.domain.entities;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.bank.domain.valueobjects.Email;

class SystemUserTest {

    @Test
    void debeCreateUserConDatosValidos() {
        SystemUser user = new SystemUser(
                1L,
                "client-1",
                "User Valido",
                "1234567890",
                new Email("user@bank.com"),
                "3001234567",
                LocalDate.now().minusYears(30),
                "Address 123",
                SystemRole.NATURAL_PERSON_CLIENT,
                UserStatus.ACTIVE
        );

        assertEquals(1L, user.getUserId());
        assertEquals("1234567890", user.getIdIdentification());
        assertEquals(UserStatus.ACTIVE, user.getUserStatus());
    }

    @Test
    void debeFallarSiIdUserNoEsPositivo() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> new SystemUser(
                0L,
                "client-1",
                "User Invalido",
                "1234567890",
                new Email("user@bank.com"),
                "3001234567",
                LocalDate.now().minusYears(30),
                "Address 123",
                SystemRole.NATURAL_PERSON_CLIENT,
                UserStatus.ACTIVE
        ));
        assertEquals("Invalid user id", thrown.getMessage());
    }

    @Test
    void debeFallarSiIdentificationSuperaLongitudMaxima() {
        String identificationLarga = "123456789012345678901";

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> new SystemUser(
                2L,
                "client-1",
                "User Invalido",
                identificationLarga,
                new Email("user@bank.com"),
                "3001234567",
                LocalDate.now().minusYears(30),
                "Address 123",
                SystemRole.NATURAL_PERSON_CLIENT,
                UserStatus.ACTIVE
        ));
        assertEquals("Identification required", thrown.getMessage());
    }

    @Test
    void debeFallarSiPhoneNoEsNumerico() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> new SystemUser(
                3L,
                "client-1",
                "User Invalido",
                "1234567890",
                new Email("user@bank.com"),
                "30012ABC",
                LocalDate.now().minusYears(30),
                "Address 123",
                SystemRole.NATURAL_PERSON_CLIENT,
                UserStatus.ACTIVE
        ));
        assertEquals("Invalid phone", thrown.getMessage());
    }

    @Test
    void debeFallarSiPersonaNaturalEsMenorDeEdad() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> new SystemUser(
                4L,
                "client-1",
                "User Menor",
                "1234567890",
                new Email("user@bank.com"),
                "3001234567",
                LocalDate.now().minusYears(17),
                "Address 123",
                SystemRole.NATURAL_PERSON_CLIENT,
                UserStatus.ACTIVE
        ));
        assertEquals("User must be an adult", thrown.getMessage());
    }

    @Test
    void debeFallarSiIdRelatedEsObligatorioPorRole() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> new SystemUser(
                5L,
                null,
                "Supervisor",
                "1234567890",
                new Email("supervisor@bank.com"),
                "3001234567",
                null,
                "Address 123",
                SystemRole.COMPANY_SUPERVISOR,
                UserStatus.ACTIVE
        ));
        assertEquals("Related ID is required for role COMPANY_SUPERVISOR", thrown.getMessage());
    }

    @Test
    void debeFallarSiEmpleadoNoRegistraFechaNacimiento() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> new SystemUser(
                6L,
                null,
                "Teller User",
                "99999111",
                new Email("teller@bank.com"),
                "3001234567",
                null,
                "Address 123",
                SystemRole.TELLER_EMPLOYEE,
                UserStatus.ACTIVE
        ));
        assertEquals("Birth date is required for natural person users", thrown.getMessage());
    }

    @Test
    void debePermitirClienteEmpresaSinFechaNacimiento() {
        SystemUser user = new SystemUser(
                7L,
                "company-1",
                "Company User",
                "NIT-123456",
                new Email("company@bank.com"),
                "3001234567",
                null,
                "Corporate Address",
                SystemRole.BUSINESS_CLIENT,
                UserStatus.ACTIVE
        );

        assertEquals(SystemRole.BUSINESS_CLIENT, user.getSystemRole());
    }
}
