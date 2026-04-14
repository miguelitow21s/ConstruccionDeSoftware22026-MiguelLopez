package com.bank.application.usecases;

import org.springframework.stereotype.Service;

import com.bank.application.services.AuthContextService;
import com.bank.application.services.CompanyOperationalUsersManagementService;
import com.bank.application.services.CompanyOperationalUsersManagementService.CompanyOperationalUser;

@Service
public class RegisterCompanyOperationalUserUseCase {

    private final CompanyOperationalUsersManagementService managementUsersService;
    private final AuthContextService authContextService;

    public RegisterCompanyOperationalUserUseCase(CompanyOperationalUsersManagementService managementUsersService,
                                                   AuthContextService authContextService) {
        this.managementUsersService = managementUsersService;
        this.authContextService = authContextService;
    }

    public CompanyOperationalUser execute(String username, String fullName, String email) {
        validateSupervisor();
        return managementUsersService.register(
                authContextService.currentRelatedClientIdOrThrow(),
                username,
                fullName,
                email
        );
    }

    private void validateSupervisor() {
        if (!authContextService.hasRole("COMPANY_SUPERVISOR")) {
            throw new SecurityException("Only Company Supervisor can manage operational users");
        }
    }
}
