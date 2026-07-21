package com.jamil.ahadith.features.account.repository;

import com.jamil.ahadith.features.account.entity.PasswordResetToken;
import com.jamil.ahadith.features.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    @Query(value = "select * from password_reset_tokens where token_hash = :tokenHash for update", nativeQuery = true)
    Optional<PasswordResetToken> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

    List<PasswordResetToken> findByUserAndConsumedAtIsNullOrderByCreatedAtDesc(User user);

    @Modifying(flushAutomatically = true)
    @Query("update PasswordResetToken t set t.consumedAt = :consumedAt where t.user = :user and t.consumedAt is null")
    int consumeActiveForUser(@Param("user") User user, @Param("consumedAt") Instant consumedAt);
}
