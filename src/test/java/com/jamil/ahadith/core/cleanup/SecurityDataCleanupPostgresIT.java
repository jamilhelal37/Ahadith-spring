package com.jamil.ahadith.core.cleanup;

import com.jamil.ahadith.core.PostgresIntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = {
        "app.cleanup.enabled=true",
        "app.cleanup.login-attempt-retention=2d",
        "app.cleanup.batch-size=10"
})
class SecurityDataCleanupPostgresIT extends PostgresIntegrationTestBase {
    @Autowired
    private SecurityDataCleanupService cleanupService;

    @Test
    void scheduledCleanupShouldUsePostgresNativeDeleteOldLoginAttempts() {
        String recentEmailKey = uniqueKey("recent");
        String staleEmailKey = uniqueKey("stale");
        insertAttempt(recentEmailKey, Instant.now().minus(Duration.ofHours(1)), null);
        insertAttempt(staleEmailKey, Instant.now().minus(Duration.ofDays(5)), Instant.now().minus(Duration.ofDays(1)));

        cleanupService.scheduledCleanup();

        assertThat(attemptExists(recentEmailKey)).isTrue();
        assertThat(attemptExists(staleEmailKey)).isFalse();
    }

    private void insertAttempt(String emailKey, Instant updatedAt, Instant lockedUntil) {
        jdbc.update(
                """
                insert into public.login_attempts (
                    email_key,
                    ip_address,
                    failed_count,
                    locked_until,
                    last_failed_at,
                    created_at,
                    updated_at
                )
                values (?, '127.0.0.1', 1, ?, ?, ?, ?)
                """,
                emailKey,
                lockedUntil == null ? null : Timestamp.from(lockedUntil),
                Timestamp.from(updatedAt),
                Timestamp.from(updatedAt),
                Timestamp.from(updatedAt)
        );
    }

    private boolean attemptExists(String emailKey) {
        Long count = jdbc.queryForObject(
                "select count(*) from public.login_attempts where email_key = ?",
                Long.class,
                emailKey
        );
        return count != null && count > 0;
    }

    private String uniqueKey(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }
}
