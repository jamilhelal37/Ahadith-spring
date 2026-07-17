package com.jamil.ahadith.core.security.filter;

import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.user.entity.UserStatus;
import com.jamil.ahadith.features.user.repository.UserRepository;
import com.jamil.ahadith.core.security.jwt.JwtService;
import com.jamil.ahadith.core.security.SecurityRoleUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String CLAIM_TOKEN_TYPE = "tokenType";

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected boolean shouldNotFilter(
            @NonNull HttpServletRequest request
    ) {
        String path = resolveRequestPath(request);

        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || path.equals("/auth")
                || path.startsWith("/auth/")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.equals("/swagger-ui.html");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!hasBearerToken(authorizationHeader)) {
            filterChain.doFilter(request, response);
            return;
        }
        Authentication currentAuthentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (currentAuthentication != null
                && currentAuthentication.isAuthenticated()) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = extractToken(authorizationHeader);

        if (token.isBlank()) {
            writeUnauthorized(
                    response,
                    "Bearer token is missing"
            );
            return;
        }

        Claims claims;

        try {
            claims = jwtService.parseClaims(token);
        } catch (JwtException | IllegalArgumentException exception) {
            writeUnauthorized(
                    response,
                    "Invalid or expired access token"
            );
            return;
        }

        String tokenType =
                claims.get(CLAIM_TOKEN_TYPE, String.class);

        if (!ACCESS_TOKEN_TYPE.equals(tokenType)) {
            writeUnauthorized(
                    response,
                    "Invalid token type"
            );
            return;
        }

        String email = claims.getSubject();

        if (email == null || email.isBlank()) {
            writeUnauthorized(
                    response,
                    "Token subject is missing"
            );
            return;
        }

        Optional<User> optionalUser =
                userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            writeUnauthorized(
                    response,
                    "Authentication failed"
            );
            return;
        }

        User user = optionalUser.get();
        if (user.getStatus() != UserStatus.active) {
            writeUnauthorized(
                    response,
                    "Authentication failed"
            );
            return;
        }

        if (user.getType() == null) {
            writeUnauthorized(
                    response,
                    "Authentication failed"
            );
            return;
        }

        var authorities = List.of(
                new SimpleGrantedAuthority(
                        SecurityRoleUtils.authority(user.getType())
                )
        );

        var authentication =
                new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
                        null,
                        authorities
                );
        authentication.setDetails(
                new WebAuthenticationDetailsSource()
                        .buildDetails(request)
        );
        SecurityContext securityContext =
                SecurityContextHolder.createEmptyContext();

        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        filterChain.doFilter(request, response);
    }
    private boolean hasBearerToken(String authorizationHeader) {
        return authorizationHeader != null
                && authorizationHeader.startsWith(BEARER_PREFIX);
    }

    private String extractToken(String authorizationHeader) {
        return authorizationHeader
                .substring(BEARER_PREFIX.length())
                .trim();
    }

    private String resolveRequestPath(HttpServletRequest request) {
        String path = request.getServletPath();

        if (path != null && !path.isBlank()) {
            return path;
        }

        path = request.getRequestURI();
        String contextPath = request.getContextPath();

        if (contextPath != null
                && !contextPath.isBlank()
                && path.startsWith(contextPath)) {
            return path.substring(contextPath.length());
        }

        return path;
    }

    private void writeUnauthorized(
            HttpServletResponse response,
            String message
    ) throws IOException {

        SecurityContextHolder.clearContext();

        response.setStatus(
                HttpServletResponse.SC_UNAUTHORIZED
        );

        response.setContentType(
                MediaType.APPLICATION_JSON_VALUE
        );

        response.setCharacterEncoding(
                StandardCharsets.UTF_8.name()
        );

        response.getWriter().write(
                """
                {
                  "status": 401,
                  "error": "Unauthorized",
                  "message": "%s"
                }
                """.formatted(message)
        );
    }
}
