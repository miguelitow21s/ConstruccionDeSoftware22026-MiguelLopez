package com.bank.application.usecases;

import com.bank.application.ports.PrestamoRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.Prestamo;
import org.springframework.stereotype.Service;

import java.util.List;

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
        if (authContextService.hasRole("ANALISTA") || authContextService.hasRole("COMERCIAL")) {
            return prestamoRepository.findAll();
        }

        if (authContextService.hasAnyRole("CLIENTE_NATURAL", "CLIENTE_EMPRESA", "EMPLEADO_EMPRESA", "SUPERVISOR_EMPRESA")) {
            return prestamoRepository.findByClienteSolicitanteId(authContextService.currentRelatedClientIdOrThrow());
        }

        return prestamoRepository.findAll();
    }
}
