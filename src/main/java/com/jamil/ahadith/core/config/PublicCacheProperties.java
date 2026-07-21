package com.jamil.ahadith.core.config;

import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Data
@Validated
@ConfigurationProperties(prefix = "app.public-cache")
public class PublicCacheProperties {
    @Min(1)
    private long referenceMaxSize = 128;

    private Duration referenceExpireAfterWrite = Duration.ofMinutes(30);
}
