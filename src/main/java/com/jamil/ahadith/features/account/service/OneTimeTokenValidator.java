package com.jamil.ahadith.features.account.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class OneTimeTokenValidator {

    public void validate(Instant consumedAt, Instant expiresAt, Instant now, String errorMessage) {
        if (consumedAt != null) {
            throw new BadCredentialsException(errorMessage);
        }

        if (expiresAt == null) {
            throw new BadCredentialsException(errorMessage);
        }

        // expiresAt != null && !expiresAt.isAfter(now)
        if (!expiresAt.isAfter(now)) {
            throw new BadCredentialsException(errorMessage);
        }
    }
}
