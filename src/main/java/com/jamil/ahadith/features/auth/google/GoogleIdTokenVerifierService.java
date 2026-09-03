package com.jamil.ahadith.features.auth.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.jamil.ahadith.core.config.GoogleAuthProperties;
import com.jamil.ahadith.core.exception.ServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoogleIdTokenVerifierService implements GoogleIdentityVerifier {
    private static final String INVALID_GOOGLE_ID_TOKEN_MESSAGE = "Invalid Google ID token";
    private static final String GOOGLE_KEYS_UNAVAILABLE_MESSAGE =
            "Google identity verification is temporarily unavailable";
    private static final List<String> GOOGLE_ISSUERS = List.of(
            "accounts.google.com",
            "https://accounts.google.com"
    );

    private final GoogleAuthProperties properties;
    private volatile GoogleIdTokenVerifier verifier;

    @Override
    public GoogleIdentity verify(String idToken) {
        if (!properties.isEnabled()) {
            throw new ServiceUnavailableException("Google authentication is not available");
        }

        try {
            GoogleIdToken verifiedToken = googleVerifier().verify(idToken);
            if (verifiedToken == null) {
                throw invalidToken();
            }

            GoogleIdToken.Payload payload = verifiedToken.getPayload();
            validatePayload(payload);

            return new GoogleIdentity(
                    trimToNull(payload.getSubject()),
                    trimToNull(payload.getEmail()),
                    Boolean.TRUE.equals(payload.getEmailVerified()),
                    trimToNull(stringClaim(payload, "name")),
                    trimToNull(stringClaim(payload, "picture"))
            );
        } catch (IOException ex) {
            throw new ServiceUnavailableException(GOOGLE_KEYS_UNAVAILABLE_MESSAGE);
        } catch (GeneralSecurityException | IllegalArgumentException ex) {
            throw invalidToken();
        }
    }

    private GoogleIdTokenVerifier googleVerifier() {
        GoogleIdTokenVerifier current = verifier;
        if (current != null) {
            return current;
        }

        synchronized (this) {
            if (verifier == null) {
                verifier = new GoogleIdTokenVerifier.Builder(
                        new NetHttpTransport(),
                        GsonFactory.getDefaultInstance()
                )
                        .setAudience(properties.audiences())
                        .build();
            }
            return verifier;
        }
    }

    private void validatePayload(GoogleIdToken.Payload payload) {
        if (payload == null) {
            throw invalidToken();
        }
        if (!GOOGLE_ISSUERS.contains(payload.getIssuer())) {
            throw invalidToken();
        }
        if (payload.getExpirationTimeSeconds() == null
                || payload.getExpirationTimeSeconds() <= Instant.now().getEpochSecond()) {
            throw invalidToken();
        }
        if (!Boolean.TRUE.equals(payload.getEmailVerified())) {
            throw invalidToken();
        }
        if (trimToNull(payload.getSubject()) == null || trimToNull(payload.getEmail()) == null) {
            throw invalidToken();
        }
    }

    private String stringClaim(GoogleIdToken.Payload payload, String claim) {
        Object value = payload.get(claim);
        return value instanceof String string ? string : null;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private BadCredentialsException invalidToken() {
        return new BadCredentialsException(INVALID_GOOGLE_ID_TOKEN_MESSAGE);
    }
}
