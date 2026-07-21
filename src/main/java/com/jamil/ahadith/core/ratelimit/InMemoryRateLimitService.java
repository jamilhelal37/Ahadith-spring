package com.jamil.ahadith.core.ratelimit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

@Service
public class InMemoryRateLimitService implements RateLimitService {
    private final RateLimitProperties properties;
    private final MeterRegistry meterRegistry;
    private final Clock clock;
    private final Cache<String, BucketState> buckets;

    @Autowired
    public InMemoryRateLimitService(
            RateLimitProperties properties,
            MeterRegistry meterRegistry
    ) {
        this(properties, meterRegistry, Clock.systemUTC());
    }

    InMemoryRateLimitService(
            RateLimitProperties properties,
            MeterRegistry meterRegistry,
            Clock clock
    ) {
        this.properties = properties;
        this.meterRegistry = meterRegistry;
        this.clock = clock;
        this.buckets = Caffeine.newBuilder()
                .maximumSize(properties.getCacheMaxSize())
                .expireAfterAccess(properties.getCacheExpireAfterAccess())
                .build();
    }

    @Override
    public RateLimitResult tryConsume(String policyName, String key) {
        if (!properties.isEnabled()) {
            return RateLimitResult.success();
        }

        RateLimitPolicy policy = properties.policy(policyName);
        if (policy == null || !policy.isEnabled()) {
            return RateLimitResult.success();
        }

        String safeKey = Objects.requireNonNullElse(key, "unknown");
        BucketState state = buckets.get(policyName + "::" + safeKey, ignored -> new BucketState(clock.instant()));
        RateLimitResult result = state.consume(policy, clock.instant());
        if (!result.allowed()) {
            meterRegistry.counter("app.rate_limit.rejections", "policy", policyName).increment();
        }
        return result;
    }

    private static final class BucketState {
        private Instant windowStartedAt;
        private long count;

        private BucketState(Instant windowStartedAt) {
            this.windowStartedAt = windowStartedAt;
        }

        private synchronized RateLimitResult consume(RateLimitPolicy policy, Instant now) {
            Duration window = policy.getWindow();
            if (!now.isBefore(windowStartedAt.plus(window))) {
                windowStartedAt = now;
                count = 0;
            }

            if (count < policy.getCapacity()) {
                count++;
                return RateLimitResult.success();
            }

            Duration retryAfter = Duration.between(now, windowStartedAt.plus(window));
            if (retryAfter.isNegative() || retryAfter.isZero()) {
                retryAfter = Duration.ofSeconds(1);
            }
            return RateLimitResult.rejected(retryAfter);
        }
    }
}
