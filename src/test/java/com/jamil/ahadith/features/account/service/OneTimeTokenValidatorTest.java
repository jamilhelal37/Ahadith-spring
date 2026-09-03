package com.jamil.ahadith.features.account.service;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OneTimeTokenValidatorTest {

    private final OneTimeTokenValidator validator = new OneTimeTokenValidator();
    private final String ERROR_MESSAGE = "Invalid token";

    @Test
    void validate_ValidToken_ShouldNotThrow() {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(60);
        
        assertDoesNotThrow(() -> validator.validate(null, expiresAt, now, ERROR_MESSAGE));
    }

    @Test
    void validate_ConsumedToken_ShouldThrow() {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(60);
        Instant consumedAt = now.minusSeconds(10);
        
        assertThrows(BadCredentialsException.class, 
            () -> validator.validate(consumedAt, expiresAt, now, ERROR_MESSAGE));
    }

    @Test
    void validate_NullExpiresAt_ShouldThrow() {
        Instant now = Instant.now();
        
        assertThrows(BadCredentialsException.class, 
            () -> validator.validate(null, null, now, ERROR_MESSAGE));
    }

    @Test
    void validate_ExpiredBeforeNow_ShouldThrow() {
        Instant now = Instant.now();
        Instant expiresAt = now.minusSeconds(1);
        
        assertThrows(BadCredentialsException.class, 
            () -> validator.validate(null, expiresAt, now, ERROR_MESSAGE));
    }

    @Test
    void validate_ExpiresExactlyNow_ShouldThrow() {
        Instant now = Instant.now();
        Instant expiresAt = now;
        
        assertThrows(BadCredentialsException.class, 
            () -> validator.validate(null, expiresAt, now, ERROR_MESSAGE));
    }

    @Test
    void validate_ExpiresInFuture_ShouldNotThrow() {
        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(1);
        
        assertDoesNotThrow(() -> validator.validate(null, expiresAt, now, ERROR_MESSAGE));
    }
}
