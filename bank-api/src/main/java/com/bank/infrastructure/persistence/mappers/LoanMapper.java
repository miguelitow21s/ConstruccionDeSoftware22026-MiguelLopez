package com.bank.infrastructure.persistence.mappers;

import org.springframework.stereotype.Component;

import com.bank.domain.entities.Loan;
import com.bank.infrastructure.persistence.entities.LoanJpaEntity;

// Mapper de préstamos. Convierte en ambas direcciones: dominio → JPA para guardar,
// JPA → dominio para devolver al use case después de leer de la base de datos.
@Component
public class LoanMapper {

    public LoanJpaEntity toJpa(Loan domain) {
        LoanJpaEntity entity = new LoanJpaEntity();
        entity.setId(domain.getId());
        entity.setLoanType(domain.getLoanType());
        entity.setApplicantClientId(domain.getApplicantClientId());
        entity.setApplicantClientIdentification(domain.getApplicantClientIdentification());
        entity.setRequestedAmount(domain.getRequestedAmount());
        entity.setApprovedAmount(domain.getApprovedAmount());
        entity.setInterestRate(domain.getInterestRate());
        entity.setTermMonths(domain.getTermMonths());
        entity.setStatus(domain.getStatus());
        entity.setApprovalDate(domain.getApprovalDate());
        entity.setDisbursementDate(domain.getDisbursementDate());
        entity.setDisbursementDestinationAccount(domain.getDisbursementDestinationAccount());
        return entity;
    }

    public Loan toDomain(LoanJpaEntity entity) {
        return new Loan(
                entity.getId(),
                entity.getLoanType(),
                entity.getApplicantClientId(),
                entity.getApplicantClientIdentification(),
                entity.getRequestedAmount(),
                entity.getApprovedAmount(),
                entity.getInterestRate(),
                entity.getTermMonths(),
                entity.getStatus(),
                entity.getApprovalDate(),
                entity.getDisbursementDate(),
                entity.getDisbursementDestinationAccount()
        );
    }
}