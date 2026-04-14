package com.bank.infrastructure.persistence.entities;

import com.bank.domain.entities.ProductCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "banking_products")
public class BankingProductJpaEntity {

    @Id
    @Column(length = 10)
    private String productCode;

    @Column(nullable = false, length = 100)
    private String nameProducto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ProductCategory categoria;

    @Column(nullable = false)
    private boolean requiresApproval;

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getNameProducto() {
        return nameProducto;
    }

    public void setNameProducto(String nameProducto) {
        this.nameProducto = nameProducto;
    }

    public ProductCategory getCategoria() {
        return categoria;
    }

    public void setCategoria(ProductCategory categoria) {
        this.categoria = categoria;
    }

    public boolean isRequiereApproval() {
        return requiresApproval;
    }

    public void setRequiereApproval(boolean requiresApproval) {
        this.requiresApproval = requiresApproval;
    }
}
