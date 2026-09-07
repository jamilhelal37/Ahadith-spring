package com.jamil.ahadith.core.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.net.URI;

@Slf4j
@Component
public class DatabaseConnectionInfoLogger {
    private final String datasourceUrl;

    public DatabaseConnectionInfoLogger(@Value("${spring.datasource.url}") String datasourceUrl) {
        this.datasourceUrl = datasourceUrl;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logDatabaseTarget() {
        DatabaseTarget target = parse(datasourceUrl);
        log.info("Database host: {}", target.host());
        log.info("Database name: {}", target.database());
    }

    static DatabaseTarget parse(String jdbcUrl) {
        try {
            if (jdbcUrl == null || !jdbcUrl.startsWith("jdbc:postgresql://")) {
                return new DatabaseTarget("unknown", "unknown");
            }
            URI uri = URI.create(jdbcUrl.substring("jdbc:".length()));
            String host = sanitize(uri.getHost());
            String path = uri.getPath();
            String database = path == null ? null : sanitize(path.replaceFirst("^/", ""));
            return new DatabaseTarget(valueOrUnknown(host), valueOrUnknown(database));
        } catch (RuntimeException exception) {
            return new DatabaseTarget("unknown", "unknown");
        }
    }

    private static String sanitize(String value) {
        return value == null ? null : value.replace('\r', '_').replace('\n', '_');
    }

    private static String valueOrUnknown(String value) {
        return value == null || value.isBlank() ? "unknown" : value;
    }

    record DatabaseTarget(String host, String database) {
    }
}
