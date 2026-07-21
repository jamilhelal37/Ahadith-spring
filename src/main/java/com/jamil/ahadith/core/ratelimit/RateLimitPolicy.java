package com.jamil.ahadith.core.ratelimit;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;

@Getter
@Setter
public class RateLimitPolicy {
    private boolean enabled = true;

    @Min(1)
    private long capacity = 60;

    @NotNull
    private Duration window = Duration.ofMinutes(1);

    @AssertTrue(message = "Rate limit window must be greater than zero")
    public boolean isWindowPositive() {
        return window != null
                && !window.isZero()
                && !window.isNegative();
    }
}
