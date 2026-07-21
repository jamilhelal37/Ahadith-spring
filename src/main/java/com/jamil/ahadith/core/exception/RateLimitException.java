package com.jamil.ahadith.core.exception;

import java.time.Duration;

public class RateLimitException extends RuntimeException {
    private final String policyName;
    private final Duration retryAfter;

    public RateLimitException(String message) {
        super(message);
        this.policyName = null;
        this.retryAfter = null;
    }

    public RateLimitException(String message, String policyName, Duration retryAfter) {
        super(message);
        this.policyName = policyName;
        this.retryAfter = retryAfter;
    }

    public String getPolicyName() {
        return policyName;
    }

    public Duration getRetryAfter() {
        return retryAfter;
    }
}
