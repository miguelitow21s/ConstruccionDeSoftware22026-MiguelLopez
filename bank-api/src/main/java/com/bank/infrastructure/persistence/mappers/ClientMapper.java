package com.bank.infrastructure.persistence.mappers;

import org.springframework.stereotype.Component;

import com.bank.domain.entities.Client;
import com.bank.domain.entities.ClientType;
import com.bank.domain.valueobjects.Email;
import com.bank.infrastructure.persistence.entities.ClientJpaEntity;

@Component
public class ClientMapper {

    public ClientJpaEntity toJpa(Client domain) {
        ClientJpaEntity entity = new ClientJpaEntity();
        entity.setId(domain.getId());
        entity.setIdIdentification(domain.getIdIdentification());
        entity.setName(domain.getName());
        entity.setEmail(domain.getEmail().value());
        entity.setPhone(domain.getPhone());
        entity.setClientType(domain.getClientType().name());
        entity.setLegalRepresentativeId(domain.getLegalRepresentativeId());
        return entity;
    }

    public Client toDomain(ClientJpaEntity entity) {
        return new Client(
                entity.getId(),
                entity.getIdIdentification(),
                entity.getName(),
                new Email(entity.getEmail()),
                entity.getPhone(),
                ClientType.valueOf(entity.getClientType()),
                entity.getLegalRepresentativeId()
        );
    }
}
