package com.jamil.ahadith.core.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Optional;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {
    private final RateLimitService rateLimitService;
    private final RateLimitKeyResolver keyResolver;
    private final HandlerExceptionResolver exceptionResolver;

    public RateLimitingFilter(
            RateLimitService rateLimitService,
            RateLimitKeyResolver keyResolver,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver
    ) {
        this.rateLimitService = rateLimitService;
        this.keyResolver = keyResolver;
        this.exceptionResolver = exceptionResolver;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            if (!HttpMethod.OPTIONS.matches(request.getMethod())) {
                resolvePolicy(request).ifPresent(policyName ->
                        rateLimitService.assertAllowed(policyName, keyResolver.ipKey(request))
                );
            }
            filterChain.doFilter(request, response);
        } catch (RateLimitExceededException ex) {
            exceptionResolver.resolveException(request, response, null, ex);
        }
    }

    private Optional<String> resolvePolicy(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();
        if (matches(method, path, "POST", "/auth/register", "/api/v1/auth/register")) {
            return Optional.of("register");
        }
        if (matches(method, path, "POST", "/auth/login", "/api/v1/auth/login")) {
            return Optional.of("login");
        }
        if (matches(method, path, "POST", "/auth/forgot-password", "/api/v1/auth/forgot-password")) {
            return Optional.of("forgot-password-ip");
        }
        if (matches(method, path, "POST", "/auth/resend-verification", "/api/v1/auth/resend-verification")) {
            return Optional.of("resend-verification");
        }
        if (matches(method, path, "POST", "/auth/verify-email", "/api/v1/auth/verify-email")) {
            return Optional.of("verify-email");
        }
        if (matches(method, path, "POST", "/auth/reset-password", "/api/v1/auth/reset-password")) {
            return Optional.of("reset-password");
        }
        if (matches(method, path, "POST", "/auth/refresh", "/api/v1/auth/refresh")) {
            return Optional.of("refresh");
        }
        if (matches(method, path, "POST", "/ahadith/search", "/api/v1/ahadith/search")) {
            return Optional.of("public-hadith-search");
        }
        return Optional.empty();
    }

    private boolean matches(String actualMethod, String actualPath, String method, String legacyPath, String v1Path) {
        return method.equalsIgnoreCase(actualMethod)
                && (legacyPath.equals(actualPath) || v1Path.equals(actualPath));
    }
}
