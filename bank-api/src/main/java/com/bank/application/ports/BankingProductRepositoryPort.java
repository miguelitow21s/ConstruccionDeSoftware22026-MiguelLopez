package com.bank.application.ports;

import java.util.List;
import java.util.Optional;

import com.bank.domain.entities.BankingProduct;

public interface BankingProductRepositoryPort {

    BankingProduct save(BankingProduct bankingProduct);

    Optional<BankingProduct> findByProductCode(String productCode);

    List<BankingProduct> findAll();
}
