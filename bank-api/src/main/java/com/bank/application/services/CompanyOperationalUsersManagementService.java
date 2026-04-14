package com.bank.application.services;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class CompanyOperationalUsersManagementService {

    private final Map<String, Map<String, CompanyOperationalUser>> usersByCompany = new ConcurrentHashMap<>();

    public List<CompanyOperationalUser> list(String companyId) {
        return List.copyOf(usersByCompany
                .getOrDefault(companyId, Map.of())
                .values());
    }

    public CompanyOperationalUser register(String companyId, String username, String fullName, String email) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Operational user username is required");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Operational user full name is required");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Operational user email is required");
        }

        var usersCompany = usersByCompany.computeIfAbsent(companyId, key -> new ConcurrentHashMap<>());
        if (usersCompany.containsKey(username)) {
            throw new IllegalArgumentException("Operational user already exists for the company");
        }

        CompanyOperationalUser user = new CompanyOperationalUser(username, fullName, email, true);
        usersCompany.put(username, user);
        return user;
    }

    public CompanyOperationalUser changeStatus(String companyId, String username, boolean active) {
        var usersCompany = usersByCompany.getOrDefault(companyId, Map.of());
        CompanyOperationalUser current = usersCompany.get(username);
        if (current == null) {
            throw new IllegalArgumentException("Operational user not found for the company");
        }

        CompanyOperationalUser updated = new CompanyOperationalUser(
                current.username(),
                current.fullName(),
                current.email(),
                active
        );

        usersByCompany.get(companyId).put(username, updated);
        return updated;
    }

    public record CompanyOperationalUser(String username, String fullName, String email, boolean active) {
    }
}
