package com.bank.infrastructure.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.bank.application.ports.ProductoBancarioRepositoryPort;
import com.bank.domain.entities.CategoriaProducto;
import com.bank.domain.entities.ProductoBancario;
import com.bank.domain.entities.TipoCuenta;

@Component
public class ProductoBancarioCatalogInitializer implements CommandLineRunner {

    private final ProductoBancarioRepositoryPort productoBancarioRepository;

    public ProductoBancarioCatalogInitializer(ProductoBancarioRepositoryPort productoBancarioRepository) {
        this.productoBancarioRepository = productoBancarioRepository;
    }

    @Override
    public void run(String... args) {
        List.of(
                new ProductoBancario(TipoCuenta.AHORROS.name(), "Cuenta de Ahorros", CategoriaProducto.CUENTAS, false),
                new ProductoBancario(TipoCuenta.CORRIENTE.name(), "Cuenta Corriente", CategoriaProducto.CUENTAS, false),
                new ProductoBancario(TipoCuenta.EMPRESARIAL.name(), "Cuenta Empresarial", CategoriaProducto.CUENTAS, true),
                new ProductoBancario(TipoCuenta.PERSONAL.name(), "Cuenta Personal", CategoriaProducto.CUENTAS, false)
        ).forEach(this::guardarSiNoExiste);
    }

    private void guardarSiNoExiste(ProductoBancario producto) {
        if (productoBancarioRepository.findByCodigoProducto(producto.getCodigoProducto()).isEmpty()) {
            productoBancarioRepository.save(producto);
        }
    }
}
