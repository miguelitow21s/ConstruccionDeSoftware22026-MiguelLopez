package com.bank.infrastructure.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "clients")
public class ClientJpaEntity {

    @Id
    private String id;

    @Column(nullable = false, unique = true, length = 20)
    private String identificationId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false, length = 40)
    private String typeClient;

    @Column(length = 50)
    private String legalRepresentativeId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getIdIdentification() {
        return identificationId;
    }

    public void setIdIdentification(String identificationId) {
        this.identificationId = identificationId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getClientType() {
        return typeClient;
    }

    public void setClientType(String typeClient) {
        this.typeClient = typeClient;
    }

    public String getLegalRepresentativeId() {
        return legalRepresentativeId;
    }

    public void setLegalRepresentativeId(String legalRepresentativeId) {
        this.legalRepresentativeId = legalRepresentativeId;
    }
}
