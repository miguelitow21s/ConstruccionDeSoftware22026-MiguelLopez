package com.bank.application.usecases;

import java.util.List;

import org.springframework.stereotype.Service;

import com.bank.application.services.AuthContextService;
import com.bank.application.services.CompanyOperationalUsersManagementService;
import com.bank.application.services.CompanyOperationalUsersManagementService.CompanyOperationalUser;

@Service
public class ListCompanyOperationalUsersUseCase {

    private final CompanyOperationalUsersManagementService managementUsersService;
    private final AuthContextService authContextService;

    public ListCompanyOperationalUsersUseCase(CompanyOperationalUsersManagementService managementUsersService,
                                                  AuthContextService authContextService) {
        this.managementUsersService = managementUsersService;
        this.authContextService = authContextService;
    }

    public List<CompanyOperationalUser> execute() {
        validateSupervisor();
        return managementUsersService.list(authContextService.currentRelatedClientIdOrThrow());
    }

    private void validateSupervisor() {
        if (!authContextService.hasRole("COMPANY_SUPERVISOR")) {
            throw new SecurityException("Only Company Supervisor can manage operational users");
        }
    }
}
