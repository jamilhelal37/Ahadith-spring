package com.jamil.ahadith.features.notification.repository;

import com.jamil.ahadith.features.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
}