package com.jamil.ahadith.core.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class LegacyApiDeprecationInterceptor implements HandlerInterceptor {
    private static final Set<String> LEGACY_PREFIXES = Set.of(
            "/auth",
            "/ahadith",
            "/books",
            "/rawis",
            "/rulings",
            "/topics",
            "/muhaddiths",
            "/explaining",
            "/fake-ahadith",
            "/similar-ahadith",
            "/admin",
            "/me",
            "/scholar",
            "/filterslist"
    );

    private final ApiProperties apiProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();
        if (isLegacyPath(path)) {
            response.setHeader("Deprecation", "true");
            response.setHeader("Link", "<" + canonicalPath(path) + ">; rel=\"successor-version\"");
            if (apiProperties.getLegacySunset() != null && !apiProperties.getLegacySunset().isBlank()) {
                response.setHeader("Sunset", apiProperties.getLegacySunset());
            }
        }
        return true;
    }

    private boolean isLegacyPath(String path) {
        if (path == null || path.startsWith("/api/v1")) {
            return false;
        }
        return LEGACY_PREFIXES.stream().anyMatch(prefix -> path.equals(prefix) || path.startsWith(prefix + "/"));
    }

    private String canonicalPath(String path) {
        if (path.equals("/filterslist") || path.startsWith("/filterslist/")) {
            return "/api/v1/search/filters";
        }
        return "/api/v1" + path;
    }
}
