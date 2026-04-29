package com.bank.application.usecases;

import org.springframework.stereotype.Service;

import com.bank.application.ports.LoanRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.Loan;

@Service
public class GetLoanUseCase {

    private final LoanRepositoryPort loanRepository;
    private final AuthContextService authContextService;

    public GetLoanUseCase(LoanRepositoryPort loanRepository, AuthContextService authContextService) {
        this.loanRepository = loanRepository;
        this.authContextService = authContextService;
    }

    public Loan execute(String loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found"));

        if (authContextService.hasAnyRole("ANALYST", "SALES")) {
            return loan;
        }

        String relatedClientId = authContextService.currentRelatedClientIdOrThrow();
        if (!relatedClientId.equals(loan.getApplicantClientId())) {
            throw new SecurityException("Not authorized to access this loan");
        }
        return loan;
    }
}
