package com.bank.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/clients/**").hasAnyRole("ANALYST", "TELLER", "SALES")
                        .requestMatchers("/users/**").hasRole("ANALYST")
                        .requestMatchers("/auditLog/**").authenticated()
                        .requestMatchers("/loans/**").authenticated()
                        .requestMatchers("/accounts/**", "/transactions/**").authenticated()
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults());

        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));
        return http.build();
    }

    @Bean
    public InMemoryUserDetailsManager users() {
        UserDetails analyst = User.withUsername("analyst")
                .password("{noop}123456")
                .roles("ANALYST")
                .build();

        UserDetails teller = User.withUsername("teller")
                .password("{noop}123456")
                .roles("TELLER")
                .build();

        UserDetails sales = User.withUsername("sales")
                .password("{noop}123456")
                .roles("SALES")
                .build();

        UserDetails supervisor = User.withUsername("supervisor")
                .password("{noop}123456")
                .roles("COMPANY_SUPERVISOR")
                .build();

        UserDetails companyEmployee = User.withUsername("company_employee")
                .password("{noop}123456")
                .roles("COMPANY_EMPLOYEE")
                .build();

        UserDetails clientNatural = User.withUsername("client_natural")
                .password("{noop}123456")
                .roles("NATURAL_CLIENT")
                .build();

        UserDetails clientCompany = User.withUsername("client_company")
                .password("{noop}123456")
                .roles("BUSINESS_CLIENT")
                .build();

        return new InMemoryUserDetailsManager(
                analyst,
                teller,
                sales,
                supervisor,
                companyEmployee,
                clientNatural,
                clientCompany
        );
    }
}
