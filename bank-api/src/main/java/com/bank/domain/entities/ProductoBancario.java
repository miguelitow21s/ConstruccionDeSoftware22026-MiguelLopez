package com.bank.domain.entities;

import java.util.Objects;

public class ProductoBancario {

    private final String codigoProducto;
    private final String nombreProducto;
    private final CategoriaProducto categoria;
    private final boolean requiereAprobacion;

    public ProductoBancario(String codigoProducto,
                            String nombreProducto,
                            CategoriaProducto categoria,
                            boolean requiereAprobacion) {
        if (codigoProducto == null || codigoProducto.isBlank() || codigoProducto.length() > 10) {
            throw new IllegalArgumentException("Codigo de producto invalido");
        }
        if (nombreProducto == null || nombreProducto.isBlank() || nombreProducto.length() > 100) {
            throw new IllegalArgumentException("Nombre de producto invalido");
        }
        if (categoria == null) {
            throw new IllegalArgumentException("Categoria de producto obligatoria");
        }

        this.codigoProducto = codigoProducto;
        this.nombreProducto = nombreProducto;
        this.categoria = categoria;
        this.requiereAprobacion = requiereAprobacion;
    }

    public String getCodigoProducto() {
        return codigoProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public CategoriaProducto getCategoria() {
        return categoria;
    }

    public boolean isRequiereAprobacion() {
        return requiereAprobacion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProductoBancario that = (ProductoBancario) o;
        return Objects.equals(codigoProducto, that.codigoProducto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigoProducto);
    }
}
