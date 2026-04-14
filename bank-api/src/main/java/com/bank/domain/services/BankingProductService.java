package com.bank.domain.services;

import com.bank.domain.entities.BankingProduct;

public class BankingProductService {

    public void validateEnabledAccountType(BankingProduct bankingProduct) {
        if (bankingProduct == null) {
            throw new IllegalArgumentException("Banking product is required");
        }
    }
}
