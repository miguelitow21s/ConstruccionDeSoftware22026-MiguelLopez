package com.bank.infrastructure.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.bank.application.ports.BankingProductRepositoryPort;
import com.bank.domain.entities.ProductCategory;
import com.bank.domain.entities.BankingProduct;
import com.bank.domain.entities.AccountType;

@Component
public class BankingProductCatalogInitializer implements CommandLineRunner {

    private final BankingProductRepositoryPort bankingProductRepository;

    public BankingProductCatalogInitializer(BankingProductRepositoryPort bankingProductRepository) {
        this.bankingProductRepository = bankingProductRepository;
    }

    @Override
    public void run(String... args) {
        List.of(
                new BankingProduct(AccountType.SAVINGS.name(), "Savings Account", ProductCategory.ACCOUNTS, false),
                new BankingProduct(AccountType.CHECKING.name(), "Account Corriente", ProductCategory.ACCOUNTS, false),
                new BankingProduct(AccountType.BUSINESS.name(), "Account Companyrial", ProductCategory.ACCOUNTS, true),
                new BankingProduct(AccountType.PERSONAL.name(), "Account Personal", ProductCategory.ACCOUNTS, false)
        ).forEach(this::guardarSiNoExiste);
    }

    private void guardarSiNoExiste(BankingProduct producto) {
        if (bankingProductRepository.findByProductCode(producto.getProductCode()).isEmpty()) {
            bankingProductRepository.save(producto);
        }
    }
}
