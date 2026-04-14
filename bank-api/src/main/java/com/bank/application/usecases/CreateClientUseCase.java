package com.bank.application.usecases;

import java.time.LocalDate;
import java.time.Period;

import org.springframework.stereotype.Service;

import com.bank.application.ports.ClientRepositoryPort;
import com.bank.domain.entities.Client;
import com.bank.domain.entities.ClientType;
import com.bank.domain.valueobjects.Email;

@Service
public class CreateClientUseCase {

    private final ClientRepositoryPort clientRepository;

    public CreateClientUseCase(ClientRepositoryPort clientRepository) {
        this.clientRepository = clientRepository;
    }

    public Client execute(String identificationId,
                           String name,
                           String email,
                           String phone,
                           LocalDate birthDate,
                           String address,
                           String typeClientRaw,
                           String legalRepresentativeId) {
        clientRepository.findByIdIdentification(identificationId).ifPresent(existing -> {
            throw new IllegalArgumentException("A client with that identification already exists");
        });
        clientRepository.findByEmail(email).ifPresent(existing -> {
            throw new IllegalArgumentException("A client with that email already exists");
        });

        ClientType typeClient = (typeClientRaw == null || typeClientRaw.isBlank())
                ? ClientType.NATURAL_PERSON_CLIENT
                : ClientType.valueOf(typeClientRaw);

        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("Address is required");
        }

        if (typeClient == ClientType.NATURAL_PERSON_CLIENT) {
            if (birthDate == null) {
                throw new IllegalArgumentException("Birth date is required");
            }
            int age = Period.between(birthDate, LocalDate.now()).getYears();
            if (age < 18) {
                throw new IllegalArgumentException("Client must be an adult");
            }
        }

        if (typeClient == ClientType.BUSINESS_CLIENT) {
            if (legalRepresentativeId == null || legalRepresentativeId.isBlank()) {
                throw new IllegalArgumentException("Legal representative is required for business clients");
            }

            Client legalRepresentative = clientRepository.findById(legalRepresentativeId)
                    .orElseThrow(() -> new IllegalArgumentException("Legal representative not found"));

            if (legalRepresentative.getClientType() != ClientType.NATURAL_PERSON_CLIENT) {
                throw new IllegalArgumentException("The legal representative must be a natural person client");
            }
        }

        Client client = new Client(identificationId, name, new Email(email), phone, birthDate, address, typeClient, legalRepresentativeId);
        return clientRepository.save(client);
    }
}
