package com.bank.infrastructure.persistence.mappers;

import com.bank.domain.entities.Cuenta;
import com.bank.domain.valueobjects.Dinero;
import com.bank.domain.valueobjects.NumeroCuenta;
import com.bank.infrastructure.persistence.entities.CuentaJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class CuentaMapper {

    public CuentaJpaEntity toJpa(Cuenta domain) {
        CuentaJpaEntity entity = new CuentaJpaEntity();
        entity.setId(domain.getId());
        entity.setNumeroCuenta(domain.getNumeroCuenta().value());
        entity.setSaldo(domain.getSaldo().value());
        entity.setTipoCuenta(domain.getTipoCuenta());
        entity.setClienteId(domain.getClienteId());
        entity.setEstado(domain.getEstado());
        return entity;
    }

    public Cuenta toDomain(CuentaJpaEntity entity) {
        return new Cuenta(
                entity.getId(),
                new NumeroCuenta(entity.getNumeroCuenta()),
                new Dinero(entity.getSaldo()),
                entity.getTipoCuenta(),
                entity.getClienteId(),
                entity.getEstado()
        );
    }
}
