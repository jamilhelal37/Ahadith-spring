package com.jamil.ahadith.features.account.service;

import com.jamil.ahadith.core.config.MailConfigProperties;
import com.jamil.ahadith.core.exception.RateLimitException;
import com.jamil.ahadith.core.validation.EmailNormalizer;
import com.jamil.ahadith.core.web.dto.MessageResponseDto;
import com.jamil.ahadith.features.account.entity.EmailVerificationToken;
import com.jamil.ahadith.features.account.event.AccountEmailEvent;
import com.jamil.ahadith.features.account.repository.EmailVerificationTokenRepository;
import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.user.entity.UserStatus;
import com.jamil.ahadith.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final String INVALID_VERIFICATION_TOKEN_MESSAGE = "Invalid or expired verification token";
    private static final String VERIFICATION_RESEND_MESSAGE = "If the email is eligible, a verification message has been sent";
    private static final String VERIFICATION_RESEND_LIMIT_MESSAGE = "Please wait before requesting another verification email";

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final TokenHashService tokenHashService;
    private final MailConfigProperties mailProperties;
    private final OneTimeTokenValidator oneTimeTokenValidator;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void sendInitialVerification(User user) {
        createAndSendVerificationToken(user, Instant.now());
    }

    @Transactional
    public MessageResponseDto verifyEmail(String token) {
        Instant now = Instant.now();
        String tokenHash = tokenHashService.sha256(token);

        EmailVerificationToken verificationToken = emailVerificationTokenRepository
                .findByTokenHashForUpdate(tokenHash)
                .orElseThrow(() -> new BadCredentialsException(INVALID_VERIFICATION_TOKEN_MESSAGE));

        oneTimeTokenValidator.validate(
                verificationToken.getConsumedAt(),
                verificationToken.getExpiresAt(),
                now,
                INVALID_VERIFICATION_TOKEN_MESSAGE
        );

        User tokenUser = verificationToken.getUser();
        User user = userRepository.findByIdForUpdate(tokenUser.getId())
                .filter(this::isPendingConfirmation)
                .orElseThrow(() -> new BadCredentialsException(INVALID_VERIFICATION_TOKEN_MESSAGE));

        verificationToken.setConsumedAt(now);
        user.setStatus(UserStatus.active);

        // emailVerificationTokenRepository.save(verificationToken); // Managed via dirty checking
        // userRepository.save(user); // Managed via dirty checking

        return new MessageResponseDto("Email verified");
    }

    @Transactional
    public MessageResponseDto resendVerification(String email) {
        String normalizedEmail = EmailNormalizer.normalize(email);
        Instant now = Instant.now();

        userRepository.findByEmailForUpdate(normalizedEmail)
                .filter(this::isPendingConfirmation)
                .ifPresent(user -> resendVerificationToken(user, now));

        return new MessageResponseDto(VERIFICATION_RESEND_MESSAGE);
    }

    private void resendVerificationToken(User user, Instant now) {
        List<EmailVerificationToken> activeTokens = emailVerificationTokenRepository
                .findByUserAndConsumedAtIsNullOrderByCreatedAtDesc(user);

        assertVerificationResendAllowed(activeTokens, now);
        consumeVerificationTokens(activeTokens, now);
        createAndSendVerificationToken(user, now);
    }

    private void createAndSendVerificationToken(User user, Instant now) {
        String rawToken = tokenHashService.generateOpaqueToken();
        EmailVerificationToken verificationToken = new EmailVerificationToken();
        verificationToken.setUser(user);
        verificationToken.setTokenHash(tokenHashService.sha256(rawToken));
        verificationToken.setExpiresAt(now.plus(mailProperties.getVerificationTokenTtl()));
        verificationToken.setLastSentAt(now);

        emailVerificationTokenRepository.save(verificationToken);

        eventPublisher.publishEvent(new AccountEmailEvent(user, rawToken, AccountEmailEvent.EmailEventType.VERIFICATION));
    }

    private void assertVerificationResendAllowed(List<EmailVerificationToken> activeTokens, Instant now) {
        if (activeTokens == null || activeTokens.isEmpty()) {
            return;
        }

        EmailVerificationToken latestToken = activeTokens.getFirst();
        Instant lastSentAt = latestToken.getLastSentAt();

        if (lastSentAt == null) {
            return;
        }

        Instant nextAllowedAt = lastSentAt.plus(mailProperties.getResendThrottle());
        if (nextAllowedAt.isAfter(now)) {
            throw new RateLimitException(VERIFICATION_RESEND_LIMIT_MESSAGE);
        }
    }

    private void consumeVerificationTokens(List<EmailVerificationToken> activeTokens, Instant consumedAt) {
        if (activeTokens == null || activeTokens.isEmpty()) {
            return;
        }
        activeTokens.forEach(token -> token.setConsumedAt(consumedAt));
        // emailVerificationTokenRepository.saveAll(activeTokens); // Managed via dirty checking
    }

    private boolean isPendingConfirmation(User user) {
        return user != null && user.getStatus() == UserStatus.pending_confirmation;
    }
}
