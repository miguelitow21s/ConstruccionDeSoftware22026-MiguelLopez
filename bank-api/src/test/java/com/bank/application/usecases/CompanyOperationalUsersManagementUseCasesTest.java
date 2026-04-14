package com.bank.application.usecases;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.bank.application.services.AuthContextService;
import com.bank.application.services.CompanyOperationalUsersManagementService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompanyOperationalUsersManagementUseCasesTest {

    @Test
    void supervisorPuedeRegisterListEInactivateUserOperationalDeSuCompany() {
        CompanyOperationalUsersManagementService managementService = new CompanyOperationalUsersManagementService();
        AuthContextService authContextService = new AuthContextService("supervisor:company-1");

        RegisterCompanyOperationalUserUseCase registerUseCase =
                new RegisterCompanyOperationalUserUseCase(managementService, authContextService);
        ListCompanyOperationalUsersUseCase listUseCase =
                new ListCompanyOperationalUsersUseCase(managementService, authContextService);
        UpdateCompanyOperationalUserStatusUseCase updateUseCase =
                new UpdateCompanyOperationalUserStatusUseCase(managementService, authContextService);

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("supervisor", "123456", "ROLE_COMPANY_SUPERVISOR")
        );

        var creado = registerUseCase.execute("operational_1", "Operational Uno", "operational1@company.com");
        assertTrue(creado.active());

        List<CompanyOperationalUsersManagementService.CompanyOperationalUser> users = listUseCase.execute();
        assertEquals(1, users.size());
        assertEquals("operational_1", users.getFirst().username());

        var deactivated = updateUseCase.deactivate("operational_1");
        assertFalse(deactivated.active());

        var activado = updateUseCase.activate("operational_1");
        assertTrue(activado.active());
    }

    @Test
    void noSupervisorNoPuedeManagementarUsersOperationals() {
        CompanyOperationalUsersManagementService managementService = new CompanyOperationalUsersManagementService();
        AuthContextService authContextService = new AuthContextService("empleado_company:company-1");

        RegisterCompanyOperationalUserUseCase registerUseCase =
                new RegisterCompanyOperationalUserUseCase(managementService, authContextService);

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("empleado_company", "123456", "ROLE_COMPANY_EMPLOYEE")
        );

        SecurityException thrown = assertThrows(
                SecurityException.class,
                () -> registerUseCase.execute("operational_2", "Operational Dos", "operational2@company.com")
        );
        assertEquals("Only Company Supervisor can manage operational users", thrown.getMessage());
    }
}

