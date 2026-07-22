package com.jamil.ahadith.core.web;

import com.jamil.ahadith.core.config.SecurityProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Service
@RequiredArgsConstructor
public class ClientIpService {
    private final SecurityProperties securityProperties;

    public String resolve(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        if (securityProperties.isTrustedProxyHeaders()) {
            String forwardedFor = request.getHeader("X-Forwarded-For");
            if (forwardedFor != null && !forwardedFor.isBlank()) {
                String firstCandidate = forwardedFor.split(",", 2)[0].trim();
                if (isValidIp(firstCandidate)) {
                    return firstCandidate;
                }
            }
        }
        return request.getRemoteAddr();
    }

    private boolean isValidIp(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            InetAddress.getByName(value);
            return value.chars().allMatch(character ->
                    Character.digit(character, 16) >= 0
                            || character == '.'
                            || character == ':');
        } catch (UnknownHostException ex) {
            return false;
        }
    }
}
