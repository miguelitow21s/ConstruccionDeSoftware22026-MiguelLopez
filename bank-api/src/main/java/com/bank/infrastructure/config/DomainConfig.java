package com.bank.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.bank.domain.services.AccountService;
import com.bank.domain.services.BankingProductService;
import com.bank.domain.services.TransferService;
import com.bank.domain.services.SystemUserService;

@Configuration
public class DomainConfig {

    @Bean
    public AccountService serviceAccount() {
        return new AccountService();
    }

    @Bean
    public TransferService transferService() {
        return new TransferService();
    }

    @Bean
    public SystemUserService serviceSystemUser() {
        return new SystemUserService();
    }

    @Bean
    public BankingProductService serviceBankingProduct() {
        return new BankingProductService();
    }
}
