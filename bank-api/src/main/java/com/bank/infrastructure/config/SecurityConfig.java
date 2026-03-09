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
                        .requestMatchers("/clientes/**").hasAnyRole("ANALISTA", "VENTANILLA", "COMERCIAL")
                        .requestMatchers("/cuentas/transferencias/*/aprobar", "/cuentas/transferencias/*/rechazar")
                        .hasAnyRole("SUPERVISOR_EMPRESA", "ANALISTA")
                        .requestMatchers("/cuentas/**", "/transacciones/**").authenticated()
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults());

        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));
        return http.build();
    }

    @Bean
    public InMemoryUserDetailsManager users() {
        UserDetails analista = User.withUsername("analista")
                .password("{noop}123456")
                .roles("ANALISTA")
                .build();

        UserDetails ventanilla = User.withUsername("ventanilla")
                .password("{noop}123456")
                .roles("VENTANILLA")
                .build();

        UserDetails comercial = User.withUsername("comercial")
                .password("{noop}123456")
                .roles("COMERCIAL")
                .build();

        UserDetails supervisor = User.withUsername("supervisor")
                .password("{noop}123456")
                .roles("SUPERVISOR_EMPRESA")
                .build();

        return new InMemoryUserDetailsManager(analista, ventanilla, comercial, supervisor);
    }
}
