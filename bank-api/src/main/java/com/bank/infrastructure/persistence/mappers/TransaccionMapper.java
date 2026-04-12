package com.bank.infrastructure.persistence.mappers;

import org.springframework.stereotype.Component;

import com.bank.domain.entities.Transaccion;
import com.bank.domain.valueobjects.Dinero;
import com.bank.infrastructure.persistence.entities.TransaccionJpaEntity;

@Component
public class TransaccionMapper {

    public TransaccionJpaEntity toJpa(Transaccion domain) {
        TransaccionJpaEntity entity = new TransaccionJpaEntity();
        entity.setId(domain.getId());
        entity.setTipoTransaccion(domain.getTipoTransaccion());
        entity.setMonto(domain.getMonto().value());
        entity.setFecha(domain.getFecha());
        entity.setFechaAprobacion(domain.getFechaAprobacion());
        entity.setCuentaOrigen(domain.getCuentaOrigen());
        entity.setCuentaDestino(domain.getCuentaDestino());
        entity.setIdUsuarioCreador(domain.getIdUsuarioCreador());
        entity.setIdUsuarioAprobador(domain.getIdUsuarioAprobador());
        entity.setEstado(domain.getEstado());
        return entity;
    }

    public Transaccion toDomain(TransaccionJpaEntity entity) {
        return new Transaccion(
                entity.getId(),
                entity.getTipoTransaccion(),
                new Dinero(entity.getMonto()),
                entity.getFecha(),
                entity.getFechaAprobacion(),
                entity.getCuentaOrigen(),
                entity.getCuentaDestino(),
                entity.getEstado(),
                entity.getIdUsuarioCreador(),
                entity.getIdUsuarioAprobador()
        );
    }
}
