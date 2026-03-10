package com.bank.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bankApiOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bank API - Sistema de Gestión Bancaria")
                        .description("API RESTful para gestión de información bancaria con arquitectura DDD y Hexagonal. " +
                                "Implementa operaciones de cuentas, préstamos, transferencias y bitácora de auditoría.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipo de Desarrollo")
                                .email("dev@bank.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(new SecurityRequirement().addList("basicAuth"))
                .components(new Components()
                        .addSecuritySchemes("basicAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("basic")
                                        .description("Autenticación básica HTTP. Roles disponibles: " +
                                                "ANALISTA, VENTANILLA, COMERCIAL, SUPERVISOR_EMPRESA, " +
                                                "EMPLEADO_EMPRESA, CLIENTE_NATURAL, CLIENTE_EMPRESA")));
    }
}
