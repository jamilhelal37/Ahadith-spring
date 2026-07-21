package com.jamil.ahadith.core.ratelimit;

import com.jamil.ahadith.core.web.ClientIpService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class RateLimitKeyResolver {
    private final ClientIpService clientIpService;

    public String ipKey(HttpServletRequest request) {
        return "ip:" + clientIpService.resolve(request);
    }

    public String emailHashKey(String email) {
        String normalized = email == null
                ? ""
                : email.trim().toLowerCase(Locale.ROOT);
        return "email-sha256:" + sha256(normalized);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }
}
