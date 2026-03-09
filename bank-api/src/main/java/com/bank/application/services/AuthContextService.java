package com.bank.application.services;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthContextService {

    private final Map<String, String> userClientMap;

    public AuthContextService(@Value("${bank.security.user-client-map:}") String rawUserClientMap) {
        this.userClientMap = parseUserClientMap(rawUserClientMap);
    }

    public String currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }

    public String currentRole() {
        return currentRoles().stream().findFirst().orElse("SYSTEM");
    }

    public Set<String> currentRoles() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities().isEmpty()) {
            return Set.of("SYSTEM");
        }
        return auth.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .collect(Collectors.toSet());
    }

    public boolean hasRole(String role) {
        return currentRoles().contains(role);
    }

    public boolean hasAnyRole(String... roles) {
        Set<String> currentRoles = currentRoles();
        return Arrays.stream(roles).anyMatch(currentRoles::contains);
    }

    public String currentRelatedClientIdOrThrow() {
        String relatedId = userClientMap.get(currentUserId());
        if (relatedId == null || relatedId.isBlank()) {
            throw new IllegalStateException("El usuario autenticado no tiene ID de cliente relacionado configurado");
        }
        return relatedId;
    }

    private Map<String, String> parseUserClientMap(String rawMap) {
        if (rawMap == null || rawMap.isBlank()) {
            return Collections.emptyMap();
        }

        Map<String, String> result = new LinkedHashMap<>();
        String[] entries = rawMap.split(",");
        for (String entry : entries) {
            String[] pair = entry.split(":", 2);
            if (pair.length == 2) {
                String user = pair[0].trim();
                String clientId = pair[1].trim();
                if (!user.isBlank() && !clientId.isBlank()) {
                    result.put(user, clientId);
                }
            }
        }
        return Collections.unmodifiableMap(result);
    }
}
