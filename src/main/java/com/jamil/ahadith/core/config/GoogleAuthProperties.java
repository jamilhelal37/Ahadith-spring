package com.jamil.ahadith.core.config;

import jakarta.validation.constraints.AssertTrue;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.google-auth")
public class GoogleAuthProperties {
    private boolean enabled = false;

    private List<String> clientIds = new ArrayList<>();

    public List<String> audiences() {
        if (clientIds == null) {
            return List.of();
        }
        return clientIds.stream()
                .map(clientId -> clientId == null ? "" : clientId.trim())
                .filter(clientId -> !clientId.isBlank())
                .distinct()
                .toList();
    }

    @AssertTrue(message = "GOOGLE_AUTH_CLIENT_IDS must be configured when APP_GOOGLE_AUTH_ENABLED=true")
    public boolean isClientIdsConfiguredWhenEnabled() {
        return !enabled || !audiences().isEmpty();
    }
}
