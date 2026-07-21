package com.jamil.ahadith.core.ratelimit;

import com.jamil.ahadith.core.exception.RateLimitException;

import java.time.Duration;

public class RateLimitExceededException extends RateLimitException {
    public RateLimitExceededException(String policyName, Duration retryAfter) {
        super("Too many requests. Try again later.", policyName, retryAfter);
    }
}
