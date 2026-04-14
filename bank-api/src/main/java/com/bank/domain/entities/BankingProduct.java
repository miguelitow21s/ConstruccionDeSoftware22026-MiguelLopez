package com.bank.domain.entities;

import java.util.Objects;

public class BankingProduct {

    private final String productCode;
    private final String nameProducto;
    private final ProductCategory categoria;
    private final boolean requiresApproval;

    public BankingProduct(String productCode,
                            String nameProducto,
                            ProductCategory categoria,
                            boolean requiresApproval) {
        if (productCode == null || productCode.isBlank() || productCode.length() > 10) {
            throw new IllegalArgumentException("Invalid product code");
        }
        if (nameProducto == null || nameProducto.isBlank() || nameProducto.length() > 100) {
            throw new IllegalArgumentException("Invalid product name");
        }
        if (categoria == null) {
            throw new IllegalArgumentException("Product category is required");
        }

        this.productCode = productCode;
        this.nameProducto = nameProducto;
        this.categoria = categoria;
        this.requiresApproval = requiresApproval;
    }

    public String getProductCode() {
        return productCode;
    }

    public String getNameProducto() {
        return nameProducto;
    }

    public ProductCategory getCategoria() {
        return categoria;
    }

    public boolean isRequiereApproval() {
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
