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
                        .title("Bank API - Banking Management System")
                        .description("RESTful API for banking management with DDD and Hexagonal Architecture. " +
                                "Implements account, loan, transfer, and audit log operations.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Development Team")
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
                                        .description("HTTP Basic authentication. Available roles: " +
                                                "ANALYST, TELLER, SALES, COMPANY_SUPERVISOR, " +
                                                "COMPANY_EMPLOYEE, NATURAL_CLIENT, BUSINESS_CLIENT")));
    }
}
