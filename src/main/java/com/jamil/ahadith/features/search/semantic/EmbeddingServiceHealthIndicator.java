package com.jamil.ahadith.features.search.semantic;

import com.jamil.ahadith.features.search.semantic.client.EmbeddingClient;
import com.jamil.ahadith.features.search.semantic.config.SemanticSearchProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("embeddingService")
@RequiredArgsConstructor
public class EmbeddingServiceHealthIndicator implements HealthIndicator {
    private final EmbeddingClient client;
    private final SemanticSearchProperties properties;

    @Override
    public Health health() {
        if (!properties.isEnabled()) {
            return Health.up().withDetail("enabled", false).build();
        }
        try {
            var response = client.health();
            return Health.up()
                    .withDetail("model", response.model())
                    .withDetail("modelVersion", response.modelVersion())
                    .withDetail("dimension", response.dimension())
                    .withDetail("device", response.device())
                    .build();
        } catch (RuntimeException ex) {
            return Health.down(ex).build();
        }
    }
}
