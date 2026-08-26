package com.jamil.ahadith.features.notification.repository;

import com.jamil.ahadith.features.user.entity.User;

import com.jamil.ahadith.features.notification.entity.UserFcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;

public interface UserFcmTokenRepository extends JpaRepository<UserFcmToken, UUID> {
    Optional<UserFcmToken> findByUserAndFcmToken(User user, String fcmToken);

    long deleteByUserAndFcmToken(User user, String fcmToken);

    long deleteByFcmTokenIn(Collection<String> fcmTokens);

    @Modifying
    @Query(value = """
            insert into public.user_fcm_tokens (
                user_id,
                fcm_token,
                last_seen
            )
            values (
                :userId,
                :fcmToken,
                :lastSeen
            )
            on conflict (fcm_token)
            do update set
                user_id = excluded.user_id,
                last_seen = excluded.last_seen,
                updated_at = current_timestamp
            """, nativeQuery = true)
    void upsertToken(@Param("userId") UUID userId,
                     @Param("fcmToken") String fcmToken,
                     @Param("lastSeen") LocalDateTime lastSeen);

    @Query("""
            select distinct token.fcmToken
            from UserFcmToken token
            join token.user user
            where user.status = com.jamil.ahadith.features.user.entity.UserStatus.active
            """)
    List<String> findDistinctTokensForActiveUsers();
}
