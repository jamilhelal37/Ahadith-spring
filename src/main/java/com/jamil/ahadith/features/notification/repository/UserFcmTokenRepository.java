package com.jamil.ahadith.features.notification.repository;

import com.jamil.ahadith.features.notification.entity.Notification;

import com.jamil.ahadith.features.user.entity.User;

import com.jamil.ahadith.features.notification.entity.UserFcmToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserFcmTokenRepository extends JpaRepository<UserFcmToken, UUID> {
}
