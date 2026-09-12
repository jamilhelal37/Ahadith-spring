package com.jamil.ahadith.features.search.semantic.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class SemanticSearchConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfiguration.class);

    @Test
    void bindsExternalEmbeddingUrlAndTimeouts() {
        contextRunner.withPropertyValues(
                        "app.semantic-search.service-url=https://example-8001.app.github.dev",
                        "app.semantic-search.connect-timeout=10s",
                        "app.semantic-search.read-timeout=120s")
                .run(context -> {
                    SemanticSearchProperties properties = context.getBean(SemanticSearchProperties.class);
                    assertThat(properties.getServiceUrl()).isEqualTo("https://example-8001.app.github.dev");
                    assertThat(properties.getConnectTimeout()).isEqualTo(Duration.ofSeconds(10));
                    assertThat(properties.getReadTimeout()).isEqualTo(Duration.ofSeconds(120));
                });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(SemanticSearchProperties.class)
    static class TestConfiguration {
    }
}
