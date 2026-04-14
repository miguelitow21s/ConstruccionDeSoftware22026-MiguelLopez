package com.bank.domain.entities;

import java.util.Objects;

public class BankingProduct {

    private final String productCode;
    private final String productName;
    private final ProductCategory category;
    private final boolean requiresApproval;

    public BankingProduct(String productCode,
                            String productName,
                            ProductCategory category,
                            boolean requiresApproval) {
        if (productCode == null || productCode.isBlank() || productCode.length() > 10) {
            throw new IllegalArgumentException("Invalid product code");
        }
        if (productName == null || productName.isBlank() || productName.length() > 100) {
            throw new IllegalArgumentException("Invalid product name");
        }
        if (category == null) {
            throw new IllegalArgumentException("Product category is required");
        }

        this.productCode = productCode;
        this.productName = productName;
        this.category = category;
        this.requiresApproval = requiresApproval;
    }

    public String getProductCode() {
        return productCode;
    }

    public String getProductName() {
        return productName;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public boolean isRequiresApproval() {
        return requiresApproval;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BankingProduct that = (BankingProduct) o;
        return Objects.equals(productCode, that.productCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productCode);
    }
}
