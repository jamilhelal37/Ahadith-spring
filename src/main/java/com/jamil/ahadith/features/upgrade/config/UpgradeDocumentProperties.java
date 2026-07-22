package com.jamil.ahadith.features.upgrade.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.upgrade.document")
public class UpgradeDocumentProperties {
    @NotNull
    private DataSize maxSize = DataSize.ofMegabytes(10);

    @Min(1)
    private int maxPages = 20;

    @NotNull
    private Duration downloadTtl = Duration.ofMinutes(5);
}
