package com.jamil.ahadith.features.notification.repository;

import com.jamil.ahadith.features.notification.entity.Notification;

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

public interface UserFcmTokenRepository extends JpaRepository<UserFcmToken, UUID> {
    Optional<UserFcmToken> findByUserAndFcmToken(User user, String fcmToken);

    long deleteByUserAndFcmToken(User user, String fcmToken);

    long deleteByFcmTokenIn(Collection<String> fcmTokens);

    @Query("""
            select distinct token.fcmToken
            from UserFcmToken token
            join token.user user
            where user.status = com.jamil.ahadith.features.user.entity.UserStatus.active
            """)
    List<String> findDistinctTokensForActiveUsers();
}
