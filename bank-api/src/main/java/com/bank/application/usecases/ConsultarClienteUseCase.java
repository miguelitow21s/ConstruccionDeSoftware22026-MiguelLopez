package com.bank.application.usecases;

import org.springframework.stereotype.Service;

import com.bank.application.ports.ClienteRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.Cliente;

@Service
public class ConsultarClienteUseCase {

    private final ClienteRepositoryPort clienteRepository;
    private final AuthContextService authContextService;

    public ConsultarClienteUseCase(ClienteRepositoryPort clienteRepository,
                                   AuthContextService authContextService) {
        this.clienteRepository = clienteRepository;
        this.authContextService = authContextService;
    }

    public Cliente execute(String clienteId) {
        if (!authContextService.hasAnyRole("ANALISTA", "COMERCIAL")) {
            throw new SecurityException("No autorizado para consultar clientes");
        }

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));

        if (authContextService.hasRole("COMERCIAL")) {
            String clienteRelacionado = authContextService.currentRelatedClientIdOrThrow();
            if (!clienteRelacionado.equals(cliente.getId())) {
                throw new SecurityException("No autorizado para consultar clientes fuera de su gestion");
            }
        }

        return cliente;
    }
}
