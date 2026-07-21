package com.jamil.ahadith.core.mail;

import com.jamil.ahadith.core.config.MailConfigProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class EmailLinkBuilder {
    private final MailConfigProperties mailProperties;

    public String verificationLink(String token) {
        return buildLink(mailProperties.getVerificationBaseUrl(), mailProperties.getVerificationPath(), token);
    }

    public String passwordResetLink(String token) {
        return buildLink(mailProperties.getFrontendBaseUrl(), "/reset-password", token);
    }

    public void validateConfiguredUrls() {
        requireAbsoluteUrl("app.mail.verification-base-url", mailProperties.getVerificationBaseUrl());
        requireAbsoluteUrl("app.mail.frontend-base-url", mailProperties.getFrontendBaseUrl());
    }

    private String buildLink(String baseUrl, String path, String token) {
        requireAbsoluteUrl("mail URL", baseUrl);
        String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8).replace("+", "%20");
        return UriComponentsBuilder.fromUriString(stripTrailingSlash(baseUrl))
                .replacePath(normalizePath(path))
                .replaceQuery("token=" + encodedToken)
                .build(true)
                .toUriString();
    }

    private void requireAbsoluteUrl(String propertyName, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(propertyName + " must be configured when email is enabled");
        }
        try {
            URI uri = new URI(value);
            if (uri.getScheme() == null || uri.getHost() == null) {
                throw new IllegalStateException(propertyName + " must be an absolute URL");
            }
        } catch (URISyntaxException ex) {
            throw new IllegalStateException(propertyName + " must be a valid absolute URL", ex);
        }
    }

    private String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            return "/verify-email";
        }
        return path.startsWith("/") ? path : "/" + path;
    }

    private String stripTrailingSlash(String value) {
        String result = value;
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }
}
