package com.jamil.ahadith.core.web;

import com.jamil.ahadith.core.config.SecurityProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
                return forwardedFor.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }
}
