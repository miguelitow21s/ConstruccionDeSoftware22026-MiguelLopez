package com.bank.application.ports;

import com.bank.domain.entities.Loan;

import java.util.List;
import java.util.Optional;

public interface LoanRepositoryPort {

    Loan save(Loan loan);

    Optional<Loan> findById(String id);

    List<Loan> findAll();

    List<Loan> findByClientApplicantId(String applicantClientId);
}
