package com.jamil.ahadith.repositories;

import com.jamil.ahadith.entities.EmailVerificationToken;
import com.jamil.ahadith.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {
    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);

    List<EmailVerificationToken> findByUserAndConsumedAtIsNullOrderByCreatedAtDesc(User user);
}
