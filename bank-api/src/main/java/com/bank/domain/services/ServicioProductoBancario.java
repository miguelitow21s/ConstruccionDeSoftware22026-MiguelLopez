package com.bank.domain.services;

import com.bank.domain.entities.ProductoBancario;

public class ServicioProductoBancario {

    public void validarTipoCuentaHabilitado(ProductoBancario productoBancario) {
        if (productoBancario == null) {
            throw new IllegalArgumentException("Producto bancario obligatorio");
        }
    }
}
