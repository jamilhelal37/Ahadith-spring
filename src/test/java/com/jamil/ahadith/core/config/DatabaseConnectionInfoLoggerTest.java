package com.jamil.ahadith.core.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseConnectionInfoLoggerTest {
    @Test
    void extractsOnlyHostAndDatabaseFromJdbcUrl() {
        var target = DatabaseConnectionInfoLogger.parse(
                "jdbc:postgresql://secret-user:secret-password@ep-example.neon.tech/neondb?sslmode=require"
        );

        assertThat(target.host()).isEqualTo("ep-example.neon.tech");
        assertThat(target.database()).isEqualTo("neondb");
        assertThat(target.toString()).doesNotContain("secret-user", "secret-password");
    }

    @Test
    void malformedUrlDoesNotExposeItsContents() {
        var target = DatabaseConnectionInfoLogger.parse("not-a-jdbc-url-containing-secret");

        assertThat(target.host()).isEqualTo("unknown");
        assertThat(target.database()).isEqualTo("unknown");
        assertThat(target.toString()).doesNotContain("secret");
    }
}
