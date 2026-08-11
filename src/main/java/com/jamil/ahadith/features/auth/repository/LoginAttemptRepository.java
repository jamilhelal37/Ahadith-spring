package com.jamil.ahadith.features.auth.repository;

import com.jamil.ahadith.features.auth.entity.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.time.Instant;
import java.util.UUID;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, UUID> {
    Optional<LoginAttempt> findByEmailKeyAndIpAddress(String emailKey, String ipAddress);

    @Modifying
    @Query(value = """
            insert into login_attempts (email_key, ip_address, failed_count, last_failed_at, locked_until)
            values (:emailKey, :ipAddress, 1, cast(:now as timestamptz), case when :maxFailures <= 1 then cast(:lockedUntil as timestamptz) else null end)
            on conflict (email_key, ip_address) do update
            set failed_count = login_attempts.failed_count + 1,
                last_failed_at = cast(:now as timestamptz),
                locked_until = case
                    when login_attempts.failed_count + 1 >= :maxFailures then cast(:lockedUntil as timestamptz)
                    else login_attempts.locked_until
                end
            """, nativeQuery = true)
    void recordFailure(@Param("emailKey") String emailKey,
                       @Param("ipAddress") String ipAddress,
                       @Param("maxFailures") int maxFailures,
                       @Param("now") Instant now,
                       @Param("lockedUntil") Instant lockedUntil);

    @Modifying
    @Query(value = """
            update login_attempts
            set failed_count = 0,
                locked_until = null,
                last_failed_at = null
            where email_key = :emailKey
              and ip_address = :ipAddress
            """, nativeQuery = true)
    void recordSuccess(@Param("emailKey") String emailKey, @Param("ipAddress") String ipAddress);

    @Modifying
    @Query(value = """
            delete from login_attempts
            where id in (
                select id
                from login_attempts
                where updated_at < :cutoff
                  and (locked_until is null or locked_until < now())
                order by updated_at asc
                limit :batchSize
            )
            """, nativeQuery = true)
    int deleteOld(@Param("cutoff") Instant cutoff, @Param("batchSize") int batchSize);
}
