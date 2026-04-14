package com.bank.interfaces.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.bank.application.usecases.ApproveLoanUseCase;
import com.bank.application.usecases.DisburseLoanUseCase;
import com.bank.application.usecases.ListLoansUseCase;
import com.bank.application.usecases.RejectLoanUseCase;
import com.bank.application.usecases.RequestLoanUseCase;
import com.bank.domain.entities.Loan;
import com.bank.interfaces.dtos.ApproveLoanRequest;
import com.bank.interfaces.dtos.DisburseLoanRequest;
import com.bank.interfaces.dtos.LoanResponse;
import com.bank.interfaces.dtos.RequestLoanRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/loans")
@Tag(name = "Loans", description = "Loan management")
@SecurityRequirement(name = "basicAuth")
public class LoanController {

    private final RequestLoanUseCase requestLoanUseCase;
    private final ApproveLoanUseCase approveLoanUseCase;
    private final RejectLoanUseCase rejectLoanUseCase;
    private final DisburseLoanUseCase disburseLoanUseCase;
    private final ListLoansUseCase listLoansUseCase;

    public LoanController(RequestLoanUseCase requestLoanUseCase,
                              ApproveLoanUseCase approveLoanUseCase,
                              RejectLoanUseCase rejectLoanUseCase,
                              DisburseLoanUseCase disburseLoanUseCase,
                              ListLoansUseCase listLoansUseCase) {
        this.requestLoanUseCase = requestLoanUseCase;
        this.approveLoanUseCase = approveLoanUseCase;
        this.rejectLoanUseCase = rejectLoanUseCase;
        this.disburseLoanUseCase = disburseLoanUseCase;
        this.listLoansUseCase = listLoansUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('NATURAL_CLIENT','BUSINESS_CLIENT','SALES')")
    @Operation(summary = "Request a loan",
               description = "Allows clients and sales employees to create a new loan request. " +
                           "The loan starts with status UNDER_REVIEW.")
    public LoanResponse request(@Valid @RequestBody RequestLoanRequest request) {
        Loan loan = requestLoanUseCase.execute(
                request.typeLoan(),
                request.applicantClientId(),
                request.requestedAmount(),
                request.interestRate(),
                request.termMonths()
        );
        return toResponse(loan);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ANALYST','SALES','COMPANY_SUPERVISOR','COMPANY_EMPLOYEE','NATURAL_CLIENT','BUSINESS_CLIENT')")
    public List<LoanResponse> list() {
        return listLoansUseCase.execute().stream().map(this::toResponse).toList();
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ANALYST')")
    @Operation(summary = "Approve loan",
               description = "Only an internal analyst can approve loans. The status changes to APPROVED.")
    public LoanResponse approve(@PathVariable String id, @Valid @RequestBody ApproveLoanRequest request) {
        return toResponse(approveLoanUseCase.execute(id, request.approvedAmount()));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ANALYST')")
    public LoanResponse reject(@PathVariable String id) {
        return toResponse(rejectLoanUseCase.execute(id));
    }

    @PostMapping("/{id}/disburse")
    @PreAuthorize("hasRole('ANALYST')")
    @Operation(summary = "Disburse loan",
               description = "Transfers the approved amount to the destination account. Available only for approved loans.")
    public LoanResponse disburse(@PathVariable String id, @Valid @RequestBody DisburseLoanRequest request) {
        return toResponse(disburseLoanUseCase.execute(id, request.destinationAccountNumber()));
    }

    private LoanResponse toResponse(Loan loan) {
        return new LoanResponse(
                loan.getId(),
                loan.getLoanType(),
                loan.getApplicantClientId(),
                loan.getRequestedAmount(),
                loan.getApprovedAmount(),
                loan.getInterestRate(),
                loan.getTermMonths(),
                loan.getStatus(),
                loan.getApprovalDate(),
                loan.getDisbursementDate(),
                loan.getDisbursementDestinationAccount()
        );
    }
}
