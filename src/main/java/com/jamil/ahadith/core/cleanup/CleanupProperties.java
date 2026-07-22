package com.jamil.ahadith.core.cleanup;

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
@ConfigurationProperties(prefix = "app.cleanup")
public class CleanupProperties {
    private boolean enabled = true;

    @NotNull
    private Duration fixedDelay = Duration.ofHours(6);

    @NotNull
    private Duration initialDelay = Duration.ofMinutes(10);

    @NotNull
    private Duration tokenRetention = Duration.ofDays(7);

    @NotNull
    private Duration loginAttemptRetention = Duration.ofDays(30);

    @Min(1)
    private int batchSize = 1_000;
}
