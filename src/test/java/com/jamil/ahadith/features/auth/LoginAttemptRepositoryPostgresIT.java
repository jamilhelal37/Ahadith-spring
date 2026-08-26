package com.jamil.ahadith.features.auth;

import com.jamil.ahadith.core.PostgresIntegrationTestBase;
import com.jamil.ahadith.features.auth.repository.LoginAttemptRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LoginAttemptRepositoryPostgresIT extends PostgresIntegrationTestBase {
    private static final String IP_ADDRESS = "127.0.0.1";

    @Autowired
    private LoginAttemptRepository loginAttemptRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    void recordFailureShouldRefreshUpdatedAtForExistingAttempt() {
        String emailKey = uniqueKey("failure");
        Instant oldTimestamp = Instant.now().minus(Duration.ofDays(5));
        insertAttempt(emailKey, IP_ADDRESS, oldTimestamp);

        transactionTemplate.executeWithoutResult(status -> loginAttemptRepository.recordFailure(
                emailKey,
                IP_ADDRESS,
                3,
                Instant.now(),
                Instant.now().plus(Duration.ofMinutes(15))
        ));

        assertThat(updatedAt(emailKey, IP_ADDRESS).toInstant())
                .isAfter(oldTimestamp)
                .isAfter(Instant.now().minus(Duration.ofMinutes(5)));
    }

    @Test
    void recordSuccessShouldRefreshUpdatedAtForExistingAttempt() {
        String emailKey = uniqueKey("success");
        Instant oldTimestamp = Instant.now().minus(Duration.ofDays(5));
        insertAttempt(emailKey, IP_ADDRESS, oldTimestamp);

        transactionTemplate.executeWithoutResult(status ->
                loginAttemptRepository.recordSuccess(emailKey, IP_ADDRESS));

        assertThat(updatedAt(emailKey, IP_ADDRESS).toInstant())
                .isAfter(oldTimestamp)
                .isAfter(Instant.now().minus(Duration.ofMinutes(5)));
        assertThat(jdbc.queryForObject(
                """
                select failed_count
                from public.login_attempts
                where email_key = ?
                  and ip_address = ?
                """,
                Integer.class,
                emailKey,
                IP_ADDRESS
        )).isZero();
    }

    @Test
    void deleteOldShouldKeepRecentlyUpdatedAttemptAndDeleteStaleAttempt() {
        String refreshedEmailKey = uniqueKey("refreshed");
        String staleEmailKey = uniqueKey("stale");
        Instant oldTimestamp = Instant.now().minus(Duration.ofDays(5));
        Instant cutoff = Instant.now().minus(Duration.ofDays(2));
        insertAttempt(refreshedEmailKey, IP_ADDRESS, oldTimestamp);
        insertAttempt(staleEmailKey, IP_ADDRESS, oldTimestamp);

        transactionTemplate.executeWithoutResult(status -> loginAttemptRepository.recordFailure(
                refreshedEmailKey,
                IP_ADDRESS,
                3,
                Instant.now(),
                Instant.now().plus(Duration.ofMinutes(15))
        ));
        int deleted = transactionTemplate.execute(status ->
                loginAttemptRepository.deleteOld(cutoff, 10));

        assertThat(deleted).isEqualTo(1);
        assertThat(attemptExists(refreshedEmailKey, IP_ADDRESS)).isTrue();
        assertThat(attemptExists(staleEmailKey, IP_ADDRESS)).isFalse();
    }

    private void insertAttempt(String emailKey, String ipAddress, Instant timestamp) {
        jdbc.update(
                """
                insert into public.login_attempts (
                    email_key,
                    ip_address,
                    failed_count,
                    last_failed_at,
                    created_at,
                    updated_at
                )
                values (?, ?, 1, ?, ?, ?)
                """,
                emailKey,
                ipAddress,
                Timestamp.from(timestamp),
                Timestamp.from(timestamp),
                Timestamp.from(timestamp)
        );
    }

    private OffsetDateTime updatedAt(String emailKey, String ipAddress) {
        return jdbc.queryForObject(
                """
                select updated_at
                from public.login_attempts
                where email_key = ?
                  and ip_address = ?
                """,
                OffsetDateTime.class,
                emailKey,
                ipAddress
        );
    }

    private boolean attemptExists(String emailKey, String ipAddress) {
        Integer count = jdbc.queryForObject(
                """
                select count(*)
                from public.login_attempts
                where email_key = ?
                  and ip_address = ?
                """,
                Integer.class,
                emailKey,
                ipAddress
        );
        return count != null && count > 0;
    }

    private String uniqueKey(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }
}
