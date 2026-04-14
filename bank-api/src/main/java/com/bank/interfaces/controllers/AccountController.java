package com.bank.interfaces.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.bank.application.usecases.ApproveTransferUseCase;
import com.bank.application.usecases.CreateAccountUseCase;
import com.bank.application.usecases.CreateBulkPaymentsUseCase;
import com.bank.application.usecases.DepositMoneyUseCase;
import com.bank.application.usecases.GetBalanceUseCase;
import com.bank.application.usecases.ListAccountsUseCase;
import com.bank.application.usecases.TransferMoneyUseCase;
import com.bank.application.usecases.WithdrawMoneyUseCase;
import com.bank.interfaces.dtos.BalanceResponse;
import com.bank.interfaces.dtos.BulkPaymentRequest;
import com.bank.interfaces.dtos.CreateAccountRequest;
import com.bank.interfaces.dtos.CreateAccountResponse;
import com.bank.interfaces.dtos.MovementRequest;
import com.bank.interfaces.dtos.TransactionResponse;
import com.bank.interfaces.dtos.TransferRequest;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/accounts")
@Tag(name = "Accounts", description = "Bank account management and transactional operations")
@SecurityRequirement(name = "basicAuth")
public class AccountController {

    private final CreateAccountUseCase createAccountUseCase;
    private final GetBalanceUseCase getBalanceUseCase;
    private final ListAccountsUseCase listAccountsUseCase;
    private final DepositMoneyUseCase depositMoneyUseCase;
    private final WithdrawMoneyUseCase withdrawMoneyUseCase;
    private final TransferMoneyUseCase transferMoneyUseCase;
    private final CreateBulkPaymentsUseCase createBulkPaymentsUseCase;
    private final ApproveTransferUseCase approveTransferUseCase;

    public AccountController(CreateAccountUseCase createAccountUseCase,
                           GetBalanceUseCase getBalanceUseCase,
                           ListAccountsUseCase listAccountsUseCase,
                           DepositMoneyUseCase depositMoneyUseCase,
                           WithdrawMoneyUseCase withdrawMoneyUseCase,
                           TransferMoneyUseCase transferMoneyUseCase,
                           CreateBulkPaymentsUseCase createBulkPaymentsUseCase,
                           ApproveTransferUseCase approveTransferUseCase) {
        this.createAccountUseCase = createAccountUseCase;
        this.getBalanceUseCase = getBalanceUseCase;
        this.listAccountsUseCase = listAccountsUseCase;
        this.depositMoneyUseCase = depositMoneyUseCase;
        this.withdrawMoneyUseCase = withdrawMoneyUseCase;
        this.transferMoneyUseCase = transferMoneyUseCase;
        this.createBulkPaymentsUseCase = createBulkPaymentsUseCase;
        this.approveTransferUseCase = approveTransferUseCase;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SALES','COMPANY_SUPERVISOR','COMPANY_EMPLOYEE','NATURAL_CLIENT','BUSINESS_CLIENT')")
    public List<CreateAccountResponse> list() {
        return listAccountsUseCase.execute().stream()
                .map(account -> new CreateAccountResponse(
                        account.getId(),
                        account.getAccountNumber().value(),
                        account.getBalance().value(),
                        account.getAccountType(),
                        account.getClientId(),
                        account.getStatus()
                ))
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ANALYST','TELLER','SALES')")
    public CreateAccountResponse create(@Valid @RequestBody CreateAccountRequest request) {
        var account = createAccountUseCase.execute(
                request.accountNumber(),
                request.initialBalance(),
                request.accountType(),
                request.clientId()
        );

        return new CreateAccountResponse(
                account.getId(),
                account.getAccountNumber().value(),
                account.getBalance().value(),
                account.getAccountType(),
                account.getClientId(),
                account.getStatus()
        );
    }

    @GetMapping("/{id}/balance")
    @PreAuthorize("hasAnyRole('ANALYST','TELLER','SALES','COMPANY_SUPERVISOR','COMPANY_EMPLOYEE','NATURAL_CLIENT','BUSINESS_CLIENT')")
    public BalanceResponse getBalance(@PathVariable String id,
                                        @RequestParam(required = false) String identificationIdClient) {
        var account = getBalanceUseCase.getAccount(id);
        var balance = getBalanceUseCase.execute(id, identificationIdClient);
        return new BalanceResponse(id, balance.value(), account.getStatus());
    }

    @PostMapping("/deposit")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('TELLER')")
    public void deposit(@Valid @RequestBody MovementRequest request) {
        depositMoneyUseCase.execute(request.accountId(), request.identificationIdClient(), request.amount());
    }

    @PostMapping("/withdraw")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('TELLER')")
    public void withdraw(@Valid @RequestBody MovementRequest request) {
        withdrawMoneyUseCase.execute(request.accountId(), request.identificationIdClient(), request.amount());
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasAnyRole('COMPANY_EMPLOYEE','NATURAL_CLIENT','BUSINESS_CLIENT')")
    public TransactionResponse transfer(@Valid @RequestBody TransferRequest request) {
        var transaction = transferMoneyUseCase.execute(
                request.sourceAccountId(),
                request.destinationAccountId(),
                request.amount(),
                request.businessOperation()
        );

        return new TransactionResponse(
                transaction.getId(),
                transaction.getTransactionType(),
                transaction.getAmount().value(),
                transaction.getDate(),
                transaction.getSourceAccount(),
                transaction.getDestinationAccount(),
                transaction.getStatus()
        );
    }

    @PostMapping("/bulk-payments")
    @PreAuthorize("hasRole('COMPANY_EMPLOYEE')")
    public List<TransactionResponse> bulkPayment(@Valid @RequestBody BulkPaymentRequest request) {
        return createBulkPaymentsUseCase.execute(
                request.sourceAccountId(),
                request.payments().stream()
                        .map(p -> new CreateBulkPaymentsUseCase.BulkPaymentItem(p.destinationAccountId(), p.amount()))
                        .toList())
                .stream()
                .map(transaction -> new TransactionResponse(
                        transaction.getId(),
                        transaction.getTransactionType(),
                        transaction.getAmount().value(),
                        transaction.getDate(),
                        transaction.getSourceAccount(),
                        transaction.getDestinationAccount(),
                        transaction.getStatus()))
                .toList();
    }

    @PostMapping("/transfers/{id}/approve")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('COMPANY_SUPERVISOR')")
    public void approveTransfer(@PathVariable String id) {
        approveTransferUseCase.approve(id);
    }

    @PostMapping("/transfers/{id}/reject")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('COMPANY_SUPERVISOR')")
    public void rejectTransfer(@PathVariable String id) {
        approveTransferUseCase.reject(id);
    }
}
