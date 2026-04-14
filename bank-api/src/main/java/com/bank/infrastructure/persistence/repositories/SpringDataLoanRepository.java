package com.bank.infrastructure.persistence.repositories;

import com.bank.infrastructure.persistence.entities.LoanJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataLoanRepository extends JpaRepository<LoanJpaEntity, String> {

	List<LoanJpaEntity> findByClientApplicantId(String applicantClientId);
}
