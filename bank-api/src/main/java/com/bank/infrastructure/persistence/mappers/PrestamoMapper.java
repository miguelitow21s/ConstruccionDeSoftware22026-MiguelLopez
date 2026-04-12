package com.bank.infrastructure.persistence.mappers;

import org.springframework.stereotype.Component;

import com.bank.domain.entities.Prestamo;
import com.bank.infrastructure.persistence.entities.PrestamoJpaEntity;

@Component
public class PrestamoMapper {

    public PrestamoJpaEntity toJpa(Prestamo domain) {
        PrestamoJpaEntity entity = new PrestamoJpaEntity();
        entity.setId(domain.getId());
        entity.setTipoPrestamo(domain.getTipoPrestamo());
        entity.setClienteSolicitanteId(domain.getClienteSolicitanteId());
        entity.setClienteSolicitanteIdentificacion(domain.getClienteSolicitanteIdentificacion());
        entity.setMontoSolicitado(domain.getMontoSolicitado());
        entity.setMontoAprobado(domain.getMontoAprobado());
        entity.setTasaInteres(domain.getTasaInteres());
        entity.setPlazoMeses(domain.getPlazoMeses());
        entity.setEstado(domain.getEstado());
        entity.setFechaAprobacion(domain.getFechaAprobacion());
        entity.setFechaDesembolso(domain.getFechaDesembolso());
        entity.setCuentaDestinoDesembolso(domain.getCuentaDestinoDesembolso());
        return entity;
    }

    public Prestamo toDomain(PrestamoJpaEntity entity) {
        return new Prestamo(
                entity.getId(),
                entity.getTipoPrestamo(),
                entity.getClienteSolicitanteId(),
                entity.getClienteSolicitanteIdentificacion(),
                entity.getMontoSolicitado(),
                entity.getMontoAprobado(),
                entity.getTasaInteres(),
                entity.getPlazoMeses(),
                entity.getEstado(),
                entity.getFechaAprobacion(),
                entity.getFechaDesembolso(),
                entity.getCuentaDestinoDesembolso()
        );
    }
}