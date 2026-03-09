package com.bank.infrastructure.persistence.mappers;

import com.bank.domain.entities.Cliente;
import com.bank.domain.valueobjects.Email;
import com.bank.infrastructure.persistence.entities.ClienteJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class ClienteMapper {

    public ClienteJpaEntity toJpa(Cliente domain) {
        ClienteJpaEntity entity = new ClienteJpaEntity();
        entity.setId(domain.getId());
        entity.setNombre(domain.getNombre());
        entity.setEmail(domain.getEmail().value());
        entity.setTelefono(domain.getTelefono());
        return entity;
    }

    public Cliente toDomain(ClienteJpaEntity entity) {
        return new Cliente(
                entity.getId(),
                entity.getNombre(),
                new Email(entity.getEmail()),
                entity.getTelefono()
        );
    }
}
