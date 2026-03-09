package com.bank.infrastructure.config;

import com.bank.domain.services.ServicioCuenta;
import com.bank.domain.services.ServicioTransferencia;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfig {

    @Bean
    public ServicioCuenta servicioCuenta() {
        return new ServicioCuenta();
    }

    @Bean
    public ServicioTransferencia servicioTransferencia() {
        return new ServicioTransferencia();
    }
}
