package com.bank.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.bank.domain.services.ServicioCuenta;
import com.bank.domain.services.ServicioProductoBancario;
import com.bank.domain.services.ServicioTransferencia;
import com.bank.domain.services.ServicioUsuarioSistema;

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

    @Bean
    public ServicioUsuarioSistema servicioUsuarioSistema() {
        return new ServicioUsuarioSistema();
    }

    @Bean
    public ServicioProductoBancario servicioProductoBancario() {
        return new ServicioProductoBancario();
    }
}
