package com.bank.application.usecases;

import java.util.List;

import org.springframework.stereotype.Service;

import com.bank.application.ports.PrestamoRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.Prestamo;

@Service
public class ListarPrestamosUseCase {

    private final PrestamoRepositoryPort prestamoRepository;
    private final AuthContextService authContextService;

    public ListarPrestamosUseCase(PrestamoRepositoryPort prestamoRepository,
                                  AuthContextService authContextService) {
        this.prestamoRepository = prestamoRepository;
        this.authContextService = authContextService;
    }

    public List<Prestamo> execute() {
        if (authContextService.hasRole("ANALISTA")) {
            return prestamoRepository.findAll();
        }

        if (authContextService.hasRole("COMERCIAL")) {
            return prestamoRepository.findByClienteSolicitanteId(authContextService.currentRelatedClientIdOrThrow());
        }

        if (authContextService.hasAnyRole("CLIENTE_NATURAL", "CLIENTE_EMPRESA", "EMPLEADO_EMPRESA", "SUPERVISOR_EMPRESA")) {
            return prestamoRepository.findByClienteSolicitanteId(authContextService.currentRelatedClientIdOrThrow());
        }

        throw new SecurityException("No autorizado para consultar prestamos");
    }
}
