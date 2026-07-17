package com.jamil.ahadith.features.auth.repository;

import com.jamil.ahadith.features.auth.entity.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, UUID> {
    Optional<LoginAttempt> findByEmailKeyAndIpAddress(String emailKey, String ipAddress);
}
