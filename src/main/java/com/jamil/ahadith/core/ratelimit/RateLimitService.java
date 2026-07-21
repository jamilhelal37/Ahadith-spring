package com.jamil.ahadith.core.ratelimit;

public interface RateLimitService {
    RateLimitResult tryConsume(String policyName, String key);

    default void assertAllowed(String policyName, String key) {
        RateLimitResult result = tryConsume(policyName, key);
        if (!result.allowed()) {
            throw new RateLimitExceededException(policyName, result.retryAfter());
        }
    }
}
