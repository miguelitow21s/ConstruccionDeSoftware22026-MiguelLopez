package com.bank.domain.entities;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;
import java.util.regex.Pattern;

import com.bank.domain.valueobjects.Email;

public class SystemUser {

    private static final int MAX_NAME = 100;
    private static final int MAX_IDENTIFICATION = 20;
    private static final int MAX_EMAIL = 100;
    private static final int MAX_PHONE = 15;
    private static final int MIN_PHONE = 7;
    private static final int MAX_ADDRESS = 200;
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{7,15}$");

    private final Long userId;
    private final String idRelated;
    private final String fullName;
    private final String identificationId;
    private final Email emailElectronico;
    private final String phone;
    private final LocalDate birthDate;
    private final String address;
    private final SystemRole systemRole;
    private UserStatus userStatus;

    public SystemUser(Long userId,
                          String idRelated,
                          String fullName,
                          String identificationId,
                          Email emailElectronico,
                          String phone,
                          LocalDate birthDate,
                          String address,
                          SystemRole systemRole,
                          UserStatus userStatus) {
                validateFields(userId, idRelated, fullName, identificationId, emailElectronico, phone, birthDate, address, systemRole, userStatus);
        this.userId = userId;
        this.idRelated = idRelated;
        this.fullName = fullName;
        this.identificationId = identificationId;
        this.emailElectronico = emailElectronico;
        this.phone = phone;
        this.birthDate = birthDate;
        this.address = address;
        this.systemRole = systemRole;
        this.userStatus = userStatus;
    }

    public void bloquear() {
        this.userStatus = UserStatus.BLOCKED;
    }

    public void deactivate() {
        this.userStatus = UserStatus.INACTIVE;
    }

    public void activate() {
        this.userStatus = UserStatus.ACTIVE;
    }

    private void validateFields(Long userId,
                               String idRelated,
                               String fullName,
                               String identificationId,
                               Email emailElectronico,
                               String phone,
                               LocalDate birthDate,
                               String address,
                               SystemRole systemRole,
                               UserStatus userStatus) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid user id");
        }
        if (fullName == null || fullName.isBlank() || fullName.length() > MAX_NAME) {
            throw new IllegalArgumentException("Full name is required");
        }
        if (identificationId == null || identificationId.isBlank() || identificationId.length() > MAX_IDENTIFICATION) {
            throw new IllegalArgumentException("Identification required");
        }
        if (emailElectronico == null || emailElectronico.value().length() > MAX_EMAIL) {
            throw new IllegalArgumentException("Email is required and must be valid");
        }
        if (phone == null || phone.length() < MIN_PHONE || phone.length() > MAX_PHONE || !PHONE_PATTERN.matcher(phone).matches()) {
            throw new IllegalArgumentException("Invalid phone");
        }
        if (address == null || address.isBlank() || address.length() > MAX_ADDRESS) {
            throw new IllegalArgumentException("Address required");
        }
        if (systemRole == null) {
            throw new IllegalArgumentException("Role is required");
        }
        if (userStatus == null) {
            throw new IllegalArgumentException("User status is required");
        }

        if (requiereIdRelated(systemRole) && (idRelated == null || idRelated.isBlank())) {
            throw new IllegalArgumentException("Related ID is required for role " + systemRole);
        }

        if (esPersonaNatural(systemRole)) {
            if (birthDate == null) {
                throw new IllegalArgumentException("Birth date is required for natural person users");
            }
            int edad = Period.between(birthDate, LocalDate.now()).getYears();
            if (edad < 18) {
                throw new IllegalArgumentException("User must be an adult");
            }
        }
    }

    private boolean esPersonaNatural(SystemRole systemRole) {
        return systemRole == SystemRole.NATURAL_PERSON_CLIENT;
    }

    private boolean requiereIdRelated(SystemRole systemRole) {
        return systemRole != SystemRole.INTERNAL_ANALYST
                && systemRole != SystemRole.TELLER_EMPLOYEE
                && systemRole != SystemRole.COMMERCIAL_EMPLOYEE
                && systemRole != SystemRole.COMPANY_EMPLOYEE;
    }

    public Long getUserId() {
        return userId;
    }

    public String getIdRelated() {
        return idRelated;
    }

    public String getNameCompleto() {
        return fullName;
    }

    public String getIdIdentification() {
        return identificationId;
    }

    public Email getEmailElectronico() {
        return emailElectronico;
    }

    public String getPhone() {
        return phone;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getAddress() {
        return address;
    }

    public SystemRole getSystemRole() {
        return systemRole;
    }

    public UserStatus getUserStatus() {
        return userStatus;
    }

    public boolean isActive() {
        return userStatus == UserStatus.ACTIVE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SystemUser that = (SystemUser) o;
        return Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
