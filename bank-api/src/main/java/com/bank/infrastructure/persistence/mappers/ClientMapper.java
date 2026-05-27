package com.bank.infrastructure.persistence.mappers;

import org.springframework.stereotype.Component;

import com.bank.domain.entities.Client;
import com.bank.domain.entities.ClientType;
import com.bank.domain.valueobjects.Email;
import com.bank.infrastructure.persistence.entities.ClientJpaEntity;

// Mapper de clientes. Nótese que Email es un value object en el dominio
// pero en base de datos se guarda como un simple String.
// El mapper hace esa conversión transparente para el resto del sistema.
@Component
public class ClientMapper {

    public ClientJpaEntity toJpa(Client domain) {
        ClientJpaEntity entity = new ClientJpaEntity();
        entity.setId(domain.getId());
        entity.setIdentificationId(domain.getIdIdentification());
        entity.setName(domain.getName());
        entity.setEmail(domain.getEmail().value());
        entity.setPhone(domain.getPhone());
        entity.setBirthDate(domain.getBirthDate());
        entity.setAddress(domain.getAddress());
        entity.setClientType(domain.getClientType().name());
        entity.setLegalRepresentativeId(domain.getLegalRepresentativeId());
        return entity;
    }

    public Client toDomain(ClientJpaEntity entity) {
        return new Client(
                entity.getId(),
                entity.getIdentificationId(),
                entity.getName(),
                new Email(entity.getEmail()),
                entity.getPhone(),
                entity.getBirthDate(),
                entity.getAddress(),
                ClientType.valueOf(entity.getClientType()),
                entity.getLegalRepresentativeId()
        );
    }
}
