package com.jamil.ahadith.features.account.repository;

import com.jamil.ahadith.features.account.entity.EmailVerificationToken;
import com.jamil.ahadith.features.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {
    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);

    @Query(value = "select * from email_verification_tokens where token_hash = :tokenHash for update", nativeQuery = true)
    Optional<EmailVerificationToken> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

    List<EmailVerificationToken> findByUserAndConsumedAtIsNullOrderByCreatedAtDesc(User user);

    @Modifying
    @Query("""
            update EmailVerificationToken token
            set token.consumedAt = :consumedAt
            where token.user = :user
              and token.consumedAt is null
            """)
    int consumeActiveForUser(@Param("user") User user, @Param("consumedAt") Instant consumedAt);

    @Modifying
    @Query(value = """
            delete from email_verification_tokens
            where id in (
                select id
                from email_verification_tokens
                where expires_at < :cutoff
                   or consumed_at < :cutoff
                order by created_at asc
                limit :batchSize
            )
            """, nativeQuery = true)
    int deleteExpiredOrConsumedBefore(@Param("cutoff") Instant cutoff, @Param("batchSize") int batchSize);
}
