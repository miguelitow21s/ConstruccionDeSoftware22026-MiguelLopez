package com.bank.application.usecases;

import java.util.List;

import org.springframework.stereotype.Service;

import com.bank.application.ports.ClientRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.Client;

@Service
public class ListClientsUseCase {

    private final ClientRepositoryPort clientRepository;
    private final AuthContextService authContextService;

    public ListClientsUseCase(ClientRepositoryPort clientRepository, AuthContextService authContextService) {
        this.clientRepository = clientRepository;
        this.authContextService = authContextService;
    }

    public List<Client> execute() {
        if (!authContextService.hasRole("ANALYST")) {
            throw new SecurityException("Only analysts can list all clients");
        }
        return clientRepository.findAll();
    }
}
