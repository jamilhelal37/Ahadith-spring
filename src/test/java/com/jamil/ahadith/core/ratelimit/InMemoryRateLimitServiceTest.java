package com.jamil.ahadith.core.ratelimit;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryRateLimitServiceTest {

    @Test
    void shouldRejectWhenPolicyCapacityIsExceededAndRecordMetric() {
        RateLimitProperties properties = new RateLimitProperties();
        RateLimitPolicy policy = new RateLimitPolicy();
        policy.setCapacity(2);
        policy.setWindow(Duration.ofMinutes(1));
        properties.setPolicies(Map.of("login", policy));
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

        InMemoryRateLimitService service = new InMemoryRateLimitService(
                properties,
                meterRegistry,
                Clock.fixed(Instant.parse("2026-07-21T18:00:00Z"), ZoneOffset.UTC)
        );

        assertThat(service.tryConsume("login", "ip:127.0.0.1").allowed()).isTrue();
        assertThat(service.tryConsume("login", "ip:127.0.0.1").allowed()).isTrue();

        RateLimitResult rejected = service.tryConsume("login", "ip:127.0.0.1");

        assertThat(rejected.allowed()).isFalse();
        assertThat(rejected.retryAfter()).isEqualTo(Duration.ofMinutes(1));
        assertThat(meterRegistry.counter("app.rate_limit.rejections", "policy", "login").count())
                .isEqualTo(1.0);
    }

    @Test
    void shouldAllowUnknownPolicyForForwardCompatibility() {
        RateLimitProperties properties = new RateLimitProperties();
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        InMemoryRateLimitService service = new InMemoryRateLimitService(properties, meterRegistry);

        assertThat(service.tryConsume("missing", "ip:127.0.0.1").allowed()).isTrue();
    }
}
