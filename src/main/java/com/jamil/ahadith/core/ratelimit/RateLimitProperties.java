package com.jamil.ahadith.core.ratelimit;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {
    private boolean enabled = true;

    @Min(1)
    private long cacheMaxSize = 10_000;

    @NotNull
    private Duration cacheExpireAfterAccess = Duration.ofHours(2);

    private Map<String, @Valid RateLimitPolicy> policies = defaultPolicies();

    public RateLimitPolicy policy(String name) {
        return policies.get(name);
    }

    @AssertTrue(message = "Rate limit cache expiration must be greater than zero")
    public boolean isCacheExpireAfterAccessPositive() {
        return cacheExpireAfterAccess != null
                && !cacheExpireAfterAccess.isZero()
                && !cacheExpireAfterAccess.isNegative();
    }

    private static Map<String, RateLimitPolicy> defaultPolicies() {
        Map<String, RateLimitPolicy> defaults = new LinkedHashMap<>();
        defaults.put("register", policy(5, Duration.ofMinutes(15)));
        defaults.put("login", policy(20, Duration.ofMinutes(1)));
        defaults.put("forgot-password-ip", policy(3, Duration.ofMinutes(15)));
        defaults.put("forgot-password-email", policy(3, Duration.ofHours(1)));
        defaults.put("resend-verification", policy(3, Duration.ofMinutes(15)));
        defaults.put("verify-email", policy(10, Duration.ofMinutes(15)));
        defaults.put("reset-password", policy(10, Duration.ofMinutes(15)));
        defaults.put("refresh", policy(30, Duration.ofMinutes(1)));
        defaults.put("public-hadith-search", policy(60, Duration.ofMinutes(1)));
        return defaults;
    }

    private static RateLimitPolicy policy(long capacity, Duration window) {
        RateLimitPolicy policy = new RateLimitPolicy();
        policy.setCapacity(capacity);
        policy.setWindow(window);
        return policy;
    }
}
