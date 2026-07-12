package com.jamil.ahadith.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.validation.annotation.Validated;

import java.time.temporal.ChronoUnit;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    @NotBlank
    private String secret;

    @NotNull
    @DurationUnit(ChronoUnit.SECONDS)
    private java.time.Duration expiration = java.time.Duration.ofHours(1);

    @NotNull
    @DurationUnit(ChronoUnit.SECONDS)
    private java.time.Duration refreshExpiration = java.time.Duration.ofDays(7);
}
