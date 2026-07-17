package com.jamil.ahadith.core.config;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

    @Min(value = 1, message = "Search history maximum must be at least 1")
    private int searchHistoryMaxPerUser = 50;

    @Min(value = 1, message = "Login maximum failures must be at least 1")
    private int loginMaxFailures = 5;

    @NotNull(message = "Login lock duration must not be null")
    private Duration loginLockDuration = Duration.ofMinutes(15);

    private boolean trustedProxyHeaders = false;

    @AssertTrue(message = "Login lock duration must be greater than zero")
    public boolean isLoginLockDurationPositive() {
        return loginLockDuration != null
                && !loginLockDuration.isZero()
                && !loginLockDuration.isNegative();
    }
}