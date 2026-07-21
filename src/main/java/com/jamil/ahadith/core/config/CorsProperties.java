package com.jamil.ahadith.core.config;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {
    @NotEmpty
    private List<String> allowedOrigins = List.of(
            "http://localhost:3000",
            "http://localhost:5173",
            "http://localhost:4200"
    );

    @NotEmpty
    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");

    @NotEmpty
    private List<String> allowedHeaders = List.of("Authorization", "Content-Type", "Accept", "X-Request-Id");

    private List<String> exposedHeaders = List.of("X-Request-Id");

    private boolean allowCredentials = false;

    @NotNull
    private Duration maxAge = Duration.ofHours(1);

    @AssertTrue(message = "CORS allowed origins must not contain wildcard '*'")
    public boolean isWildcardOriginNotAllowed() {
        return allowedOrigins == null || !allowedOrigins.contains("*");
    }
}
