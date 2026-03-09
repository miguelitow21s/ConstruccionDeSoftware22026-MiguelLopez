package com.bank.application.usecases;

import java.util.List;

import org.springframework.stereotype.Service;

import com.bank.application.ports.TransaccionRepositoryPort;
import com.bank.domain.entities.Transaccion;

@Service
public class ListarTransaccionesUseCase {

    private final TransaccionRepositoryPort transaccionRepository;

    public ListarTransaccionesUseCase(TransaccionRepositoryPort transaccionRepository) {
        this.transaccionRepository = transaccionRepository;
    }

    public List<Transaccion> execute() {
        return transaccionRepository.findAll();
    }
}
