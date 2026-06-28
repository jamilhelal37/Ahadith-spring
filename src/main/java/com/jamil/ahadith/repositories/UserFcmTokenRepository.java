package com.jamil.ahadith.repositories;

import com.jamil.ahadith.entities.UserFcmToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserFcmTokenRepository extends JpaRepository<UserFcmToken, UUID> {
}