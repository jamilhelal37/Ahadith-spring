package com.jamil.ahadith.services;

import com.jamil.ahadith.config.JwtConfig;
import com.jamil.ahadith.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    private final JwtConfig jwtConfig;

    public String generateAccessToken(User user) {
        return generateToken(user, getAccessTokenExpirationSeconds(), ACCESS_TOKEN_TYPE, true);
    }

    public String generateRefreshToken(User user) {
        return generateToken(user, getRefreshTokenExpirationSeconds(), REFRESH_TOKEN_TYPE, false);
    }

    private String generateToken(User user, long expiresInSeconds, String tokenType, boolean includeRole) {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(expiresInSeconds);

        var builder = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getEmail())
                .claim("userId", user.getId().toString())
                .claim("email", user.getEmail())
                .claim("tokenType", tokenType)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration));

        if (includeRole) {
            builder.claim("role", user.getType() != null ? SecurityRoleUtils.tokenRole(user.getType()) : null);
        }

        return builder.signWith(getSigningKey()).compact();
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public String getSubject(String token) {
        return parseClaims(token).getSubject();
    }

    public UUID getUserId(String token) {
        String userId = parseClaims(token).get("userId", String.class);
        return userId != null ? UUID.fromString(userId) : null;
    }

    public String getRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    public boolean isAccessToken(String token) {
        return isTokenType(token, ACCESS_TOKEN_TYPE);
    }

    public boolean isRefreshToken(String token) {
        return isTokenType(token, REFRESH_TOKEN_TYPE);
    }

    public long getAccessTokenExpirationSeconds() {
        return jwtConfig.getExpiration() > 0 ? jwtConfig.getExpiration() : 3600L; // 1 hour default
    }

    public long getRefreshTokenExpirationSeconds() {
        return jwtConfig.getRefreshExpiration() > 0 ? jwtConfig.getRefreshExpiration() : 604800L; // 7 days default
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isTokenType(String token, String expectedType) {
        try {
            return expectedType.equals(parseClaims(token).get("tokenType", String.class));
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    private SecretKey getSigningKey() {
        String secret = jwtConfig.getSecret();

        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT secret is not configured");
        }

        if (secret.length() < 64) {
             // For development, we might want to pad it, but it's better to enforce a strong secret.
             // I'll assume the user will provide a strong secret in application.properties.
        }

        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
