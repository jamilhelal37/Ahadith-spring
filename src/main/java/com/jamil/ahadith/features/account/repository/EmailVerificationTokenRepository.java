package com.jamil.ahadith.features.account.repository;

import com.jamil.ahadith.features.account.entity.EmailVerificationToken;
import com.jamil.ahadith.features.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {
    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);

    List<EmailVerificationToken> findByUserAndConsumedAtIsNullOrderByCreatedAtDesc(User user);
}
