package com.jamil.ahadith.repositories;

import com.jamil.ahadith.entities.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, UUID> {
    Optional<LoginAttempt> findByEmailKeyAndIpAddress(String emailKey, String ipAddress);
}
