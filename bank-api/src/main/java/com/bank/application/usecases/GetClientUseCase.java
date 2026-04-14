package com.bank.application.usecases;

import org.springframework.stereotype.Service;

import com.bank.application.ports.ClientRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.Client;

@Service
public class GetClientUseCase {

    private final ClientRepositoryPort clientRepository;
    private final AuthContextService authContextService;

    public GetClientUseCase(ClientRepositoryPort clientRepository,
                                   AuthContextService authContextService) {
        this.clientRepository = clientRepository;
        this.authContextService = authContextService;
    }

    public Client execute(String clientId) {
        if (!authContextService.hasAnyRole("ANALYST", "SALES")) {
            throw new SecurityException("Not authorized to get clients");
        }

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        if (authContextService.hasRole("SALES")) {
            String clientRelated = authContextService.currentRelatedClientIdOrThrow();
            if (!clientRelated.equals(client.getId())) {
                throw new SecurityException("Not authorized to get clients outside the authorized scope");
            }
        }

        return client;
    }
}
