package com.jamil.ahadith.features.auth.repository;

import com.jamil.ahadith.features.auth.entity.RefreshTokenSession;
import com.jamil.ahadith.features.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenSessionRepository extends JpaRepository<RefreshTokenSession, UUID> {
    Optional<RefreshTokenSession> findByTokenHash(String tokenHash);

    @Query(value = "select * from refresh_token_sessions where token_hash = :tokenHash for update", nativeQuery = true)
    Optional<RefreshTokenSession> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

    List<RefreshTokenSession> findByFamilyId(UUID familyId);

    @Modifying
    @Query("update RefreshTokenSession s set s.revokedAt = :revokedAt where s.familyId = :familyId and s.revokedAt is null")
    int revokeFamily(@Param("familyId") UUID familyId, @Param("revokedAt") Instant revokedAt);

    @Modifying
    @Query("update RefreshTokenSession s set s.revokedAt = :revokedAt where s.user = :user and s.revokedAt is null")
    int revokeAllForUser(@Param("user") User user, @Param("revokedAt") Instant revokedAt);

    @Modifying
    @Query("delete from RefreshTokenSession s where s.expiresAt < :cutoff")
    int deleteExpiredBefore(@Param("cutoff") Instant cutoff);
}
