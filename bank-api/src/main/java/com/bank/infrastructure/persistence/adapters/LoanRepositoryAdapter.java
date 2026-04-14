package com.bank.infrastructure.persistence.adapters;

import com.bank.application.ports.LoanRepositoryPort;
import com.bank.domain.entities.Loan;
import com.bank.infrastructure.persistence.mappers.LoanMapper;
import com.bank.infrastructure.persistence.repositories.SpringDataLoanRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class LoanRepositoryAdapter implements LoanRepositoryPort {

    private final SpringDataLoanRepository repository;
    private final LoanMapper mapper;

    public LoanRepositoryAdapter(SpringDataLoanRepository repository, LoanMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @SuppressWarnings("null")
    public Loan save(Loan loan) {
        return mapper.toDomain(
                Objects.requireNonNull(repository.save(mapper.toJpa(loan)))
        );
    }

    @Override
    public Optional<Loan> findById(String id) {
        return repository.findById(Objects.requireNonNull(id)).map(mapper::toDomain);
    }

    @Override
    public List<Loan> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Loan> findByClientApplicantId(String applicantClientId) {
        return repository.findByClientApplicantId(Objects.requireNonNull(applicantClientId))
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
