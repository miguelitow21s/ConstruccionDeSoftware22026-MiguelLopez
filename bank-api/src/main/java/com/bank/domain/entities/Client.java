package com.bank.domain.entities;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

import com.bank.domain.valueobjects.Email;

public class Client {

    private final String id;
    private final String identificationId;
    private final String name;
    private final Email email;
    private final String phone;
    private final LocalDate birthDate;
    private final String address;
    private final ClientType typeClient;
    private final String legalRepresentativeId;

    public Client(String identificationId, String name, Email email, String phone) {
        this(UUID.randomUUID().toString(), identificationId, name, email, phone, null, null, ClientType.NATURAL_PERSON_CLIENT, null);
    }

    public Client(String identificationId,
                   String name,
                   Email email,
                   String phone,
                   ClientType typeClient,
                   String legalRepresentativeId) {
        this(UUID.randomUUID().toString(), identificationId, name, email, phone, null, null, typeClient, legalRepresentativeId);
    }

    public Client(String identificationId,
                  String name,
                  Email email,
                  String phone,
                  LocalDate birthDate,
                  String address,
                  ClientType typeClient,
                  String legalRepresentativeId) {
        this(UUID.randomUUID().toString(), identificationId, name, email, phone, birthDate, address, typeClient, legalRepresentativeId);
    }

    public Client(String id, String identificationId, String name, Email email, String phone) {
        this(id, identificationId, name, email, phone, null, null, ClientType.NATURAL_PERSON_CLIENT, null);
    }

    public Client(String id,
                   String identificationId,
                   String name,
                   Email email,
                   String phone,
                   LocalDate birthDate,
                   String address,
                   ClientType typeClient,
                   String legalRepresentativeId) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Invalid client id");
        }
        if (identificationId == null || identificationId.isBlank() || identificationId.length() > 20) {
            throw new IllegalArgumentException("Invalid identification");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Invalid client name");
        }
        if (phone == null || phone.length() < 7 || phone.length() > 15) {
            throw new IllegalArgumentException("Invalid phone");
        }
        if (typeClient == null) {
            throw new IllegalArgumentException("Client type is required");
        }
        if (typeClient == ClientType.BUSINESS_CLIENT
                && (legalRepresentativeId == null || legalRepresentativeId.isBlank())) {
            throw new IllegalArgumentException("Legal representative is required for business clients");
        }
        this.id = id;
        this.identificationId = identificationId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.birthDate = birthDate;
        this.address = address;
        this.typeClient = typeClient;
        this.legalRepresentativeId = legalRepresentativeId;
    }

    public Client(String id,
                   String identificationId,
                   String name,
                   Email email,
                   String phone,
                   ClientType typeClient,
                   String legalRepresentativeId) {
        this(id, identificationId, name, email, phone, null, null, typeClient, legalRepresentativeId);
    }

    public String getId() {
        return id;
    }

    public String getIdIdentification() {
        return identificationId;
    }

    public String getName() {
        return name;
    }

    public Email getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public ClientType getClientType() {
        return typeClient;
    }

    public String getLegalRepresentativeId() {
        return legalRepresentativeId;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Client client = (Client) o;
        return Objects.equals(id, client.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
