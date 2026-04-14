package com.bank.application.usecases;

import java.util.List;

import org.springframework.stereotype.Service;

import com.bank.application.ports.LoanRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.Loan;

@Service
public class ListLoansUseCase {

    private final LoanRepositoryPort loanRepository;
    private final AuthContextService authContextService;

    public ListLoansUseCase(LoanRepositoryPort loanRepository,
                                  AuthContextService authContextService) {
        this.loanRepository = loanRepository;
        this.authContextService = authContextService;
    }

    public List<Loan> execute() {
        if (authContextService.hasRole("ANALYST")) {
            return loanRepository.findAll();
        }

        if (authContextService.hasRole("SALES")) {
            return loanRepository.findByClientApplicantId(authContextService.currentRelatedClientIdOrThrow());
        }

        if (authContextService.hasAnyRole("NATURAL_CLIENT", "BUSINESS_CLIENT", "COMPANY_EMPLOYEE", "COMPANY_SUPERVISOR")) {
            return loanRepository.findByClientApplicantId(authContextService.currentRelatedClientIdOrThrow());
        }

        throw new SecurityException("Not authorized to get loans");
    }
}
