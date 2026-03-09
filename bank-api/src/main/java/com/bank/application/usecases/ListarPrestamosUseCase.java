package com.bank.application.usecases;

import com.bank.application.ports.PrestamoRepositoryPort;
import com.bank.domain.entities.Prestamo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListarPrestamosUseCase {

    private final PrestamoRepositoryPort prestamoRepository;

    public ListarPrestamosUseCase(PrestamoRepositoryPort prestamoRepository) {
        this.prestamoRepository = prestamoRepository;
    }

    public List<Prestamo> execute() {
        return prestamoRepository.findAll();
    }
}
