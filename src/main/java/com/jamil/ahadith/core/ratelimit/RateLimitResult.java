package com.jamil.ahadith.core.ratelimit;

import java.time.Duration;

public record RateLimitResult(
        boolean allowed,
        Duration retryAfter
) {
    public static RateLimitResult success() {
        return new RateLimitResult(true, Duration.ZERO);
    }

    public static RateLimitResult rejected(Duration retryAfter) {
        return new RateLimitResult(false, retryAfter);
    }
}
