package com.bank.application.usecases;

import org.springframework.stereotype.Service;

import com.bank.application.services.AuthContextService;
import com.bank.application.services.CompanyOperationalUsersManagementService;
import com.bank.application.services.CompanyOperationalUsersManagementService.CompanyOperationalUser;

@Service
public class UpdateCompanyOperationalUserStatusUseCase {

    private final CompanyOperationalUsersManagementService managementUsersService;
    private final AuthContextService authContextService;

    public UpdateCompanyOperationalUserStatusUseCase(CompanyOperationalUsersManagementService managementUsersService,
                                                          AuthContextService authContextService) {
        this.managementUsersService = managementUsersService;
        this.authContextService = authContextService;
    }

    public CompanyOperationalUser activate(String username) {
        validateSupervisor();
        return managementUsersService.changeStatus(authContextService.currentRelatedClientIdOrThrow(), username, true);
    }

    public CompanyOperationalUser deactivate(String username) {
        validateSupervisor();
        return managementUsersService.changeStatus(authContextService.currentRelatedClientIdOrThrow(), username, false);
    }

    private void validateSupervisor() {
        if (!authContextService.hasRole("COMPANY_SUPERVISOR")) {
            throw new SecurityException("Only Company Supervisor can manage operational users");
        }
    }
}
