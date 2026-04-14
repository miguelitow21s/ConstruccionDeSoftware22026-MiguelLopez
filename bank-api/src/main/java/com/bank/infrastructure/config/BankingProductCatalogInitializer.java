package com.bank.infrastructure.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.bank.application.ports.BankingProductRepositoryPort;
import com.bank.domain.entities.AccountType;
import com.bank.domain.entities.BankingProduct;
import com.bank.domain.entities.ProductCategory;

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
                new BankingProduct(AccountType.CHECKING.name(), "Checking Account", ProductCategory.ACCOUNTS, false),
                new BankingProduct(AccountType.BUSINESS.name(), "Business Account", ProductCategory.ACCOUNTS, true),
                new BankingProduct(AccountType.PERSONAL.name(), "Personal Account", ProductCategory.ACCOUNTS, false)
        ).forEach(this::saveIfMissing);
    }

    private void saveIfMissing(BankingProduct product) {
        if (bankingProductRepository.findByProductCode(product.getProductCode()).isEmpty()) {
            bankingProductRepository.save(product);
        }
    }
}
