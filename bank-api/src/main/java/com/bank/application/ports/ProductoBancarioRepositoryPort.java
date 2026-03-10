package com.bank.application.ports;

import java.util.List;
import java.util.Optional;

import com.bank.domain.entities.ProductoBancario;

public interface ProductoBancarioRepositoryPort {

    ProductoBancario save(ProductoBancario productoBancario);

    Optional<ProductoBancario> findByCodigoProducto(String codigoProducto);

    List<ProductoBancario> findAll();
}
