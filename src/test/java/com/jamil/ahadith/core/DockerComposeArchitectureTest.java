package com.jamil.ahadith.core;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class DockerComposeArchitectureTest {
    @Test
    void composeUsesAnExternalEmbeddingServiceOnly() throws IOException {
        Path composePath = Path.of("docker-compose.yml");
        assumeTrue(
                Files.isRegularFile(composePath),
                "docker-compose.yml is not included in this build context"
        );

        String compose = Files.readString(composePath);

        assertThat(compose)
                .doesNotContain("context: ./embedding")
                .doesNotContain("http://embedding")
                .doesNotContain("huggingface")
                .contains("APP_EMBEDDING_SERVICE_URL: ${APP_EMBEDDING_SERVICE_URL:?");
    }
}
