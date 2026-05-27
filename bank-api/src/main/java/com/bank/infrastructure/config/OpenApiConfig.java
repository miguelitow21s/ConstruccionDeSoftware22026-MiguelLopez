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
                        .description("""
                                RESTful API for banking management with DDD and Hexagonal Architecture.

                                ## Demo users (HTTP Basic Auth)
                                | User | Password | Role |
                                |------|----------|------|
                                | analyst | 123456 | ANALYST |
                                | teller | 123456 | TELLER |
                                | sales | 123456 | SALES |
                                | supervisor | 123456 | COMPANY_SUPERVISOR |
                                | company_employee | 123456 | COMPANY_EMPLOYEE |
                                | client_natural | 123456 | NATURAL_CLIENT |
                                | client_company | 123456 | BUSINESS_CLIENT |

                                ## Pre-loaded demo data
                                - **Legal representative**: id=`representante_demo_id`, CC=`12345678`, Carlos Gomez
                                - **Company client**: id=`empresa_demo_id`, NIT=`900123456`, Tech Solutions Corp
                                - **Company account**: number=`10000001` (used as disbursement destination)

                                ## Business loan flow (company → analyst)
                                1. `POST /loans` as **client_company** → status `UNDER_REVIEW`
                                2. `POST /loans/{id}/reject` as **analyst** → status `REJECTED`
                                3. `POST /loans` as **client_company** (new request) → status `UNDER_REVIEW`
                                4. `POST /loans/{id}/approve` as **analyst** → status `APPROVED`
                                5. `POST /loans/{id}/disburse` as **analyst** → status `DISBURSED`, funds deposited to account `10000001`
                                """)
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
