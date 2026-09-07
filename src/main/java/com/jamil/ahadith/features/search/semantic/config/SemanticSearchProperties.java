package com.jamil.ahadith.features.search.semantic.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.semantic-search")
public class SemanticSearchProperties {
    private boolean enabled;
    private String serviceUrl = "http://localhost:8001";
    private String model = "BAAI/bge-m3";
    private String modelVersion = "1.3.5";
    private int batchSize = 16;
    private double minSimilarity = 0.45;
    private int candidateLimit = 100;
    private int hybridRrfK = 60;
    private Duration connectTimeout = Duration.ofSeconds(3);
    private Duration readTimeout = Duration.ofSeconds(30);
}
