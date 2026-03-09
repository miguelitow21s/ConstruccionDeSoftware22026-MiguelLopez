package com.bank.infrastructure.persistence.mappers;

import com.bank.domain.entities.Transaccion;
import com.bank.domain.valueobjects.Dinero;
import com.bank.infrastructure.persistence.entities.TransaccionJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class TransaccionMapper {

    public TransaccionJpaEntity toJpa(Transaccion domain) {
        TransaccionJpaEntity entity = new TransaccionJpaEntity();
        entity.setId(domain.getId());
        entity.setTipoTransaccion(domain.getTipoTransaccion());
        entity.setMonto(domain.getMonto().value());
        entity.setFecha(domain.getFecha());
        entity.setCuentaOrigen(domain.getCuentaOrigen());
        entity.setCuentaDestino(domain.getCuentaDestino());
        entity.setEstado(domain.getEstado());
        return entity;
    }

    public Transaccion toDomain(TransaccionJpaEntity entity) {
        return new Transaccion(
                entity.getId(),
                entity.getTipoTransaccion(),
                new Dinero(entity.getMonto()),
                entity.getFecha(),
                entity.getCuentaOrigen(),
                entity.getCuentaDestino(),
                entity.getEstado()
        );
    }
}
