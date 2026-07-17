package com.jamil.ahadith.core.config;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Getter
@Validated
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    @NotBlank(message = "JWT secret must not be blank")
    @Size(min = 64,message = "JWT secret must be at least 64 characters long")
    private final String secret;

    @NotNull(message = "JWT expiration must not be null")
    private final Duration expiration;

    @NotNull(message = "JWT refresh expiration must not be null")
    private final Duration refreshExpiration;

    public JwtConfig(
            String secret,

            @DefaultValue("1h")
            @DurationUnit(ChronoUnit.SECONDS)
            Duration expiration,

            @DefaultValue("7d")
            @DurationUnit(ChronoUnit.SECONDS)
            Duration refreshExpiration
    ) {
        this.secret = secret;
        this.expiration = expiration;
        this.refreshExpiration = refreshExpiration;
    }

    @AssertTrue(message = "JWT expiration must be greater than zero")
    public boolean isExpirationPositive() {
        return expiration != null
                && !expiration.isZero()
                && !expiration.isNegative();
    }

    @AssertTrue(message = "JWT refresh expiration must be greater than zero")
    public boolean isRefreshExpirationPositive() {
        return refreshExpiration != null
                && !refreshExpiration.isZero()
                && !refreshExpiration.isNegative();
    }

    @AssertTrue(message = "JWT refresh expiration must be longer than access token expiration")
    public boolean isRefreshExpirationLonger() {
        return expiration != null
                && refreshExpiration != null
                && refreshExpiration.compareTo(expiration) > 0;
    }
}