package com.bank.infrastructure.persistence.entities;

import com.bank.domain.entities.CategoriaProducto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "productos_bancarios")
public class ProductoBancarioJpaEntity {

    @Id
    @Column(length = 10)
    private String codigoProducto;

    @Column(nullable = false, length = 100)
    private String nombreProducto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CategoriaProducto categoria;

    @Column(nullable = false)
    private boolean requiereAprobacion;

    public String getCodigoProducto() {
        return codigoProducto;
    }

    public void setCodigoProducto(String codigoProducto) {
        this.codigoProducto = codigoProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public CategoriaProducto getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaProducto categoria) {
        this.categoria = categoria;
    }

    public boolean isRequiereAprobacion() {
        return requiereAprobacion;
    }

    public void setRequiereAprobacion(boolean requiereAprobacion) {
        this.requiereAprobacion = requiereAprobacion;
    }
}
