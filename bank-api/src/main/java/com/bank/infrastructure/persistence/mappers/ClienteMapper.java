package com.bank.infrastructure.persistence.mappers;

import org.springframework.stereotype.Component;

import com.bank.domain.entities.Cliente;
import com.bank.domain.valueobjects.Email;
import com.bank.infrastructure.persistence.entities.ClienteJpaEntity;

@Component
public class ClienteMapper {

    public ClienteJpaEntity toJpa(Cliente domain) {
        ClienteJpaEntity entity = new ClienteJpaEntity();
        entity.setId(domain.getId());
        entity.setIdIdentificacion(domain.getIdIdentificacion());
        entity.setNombre(domain.getNombre());
        entity.setEmail(domain.getEmail().value());
        entity.setTelefono(domain.getTelefono());
        return entity;
    }

    public Cliente toDomain(ClienteJpaEntity entity) {
        return new Cliente(
                entity.getId(),
            entity.getIdIdentificacion(),
                entity.getNombre(),
                new Email(entity.getEmail()),
                entity.getTelefono()
        );
    }
}
