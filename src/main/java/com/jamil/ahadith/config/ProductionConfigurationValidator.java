package com.jamil.ahadith.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class ProductionConfigurationValidator implements ApplicationRunner {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final Environment environment;
    private final JwtConfig jwtConfig;
    private final MailConfigProperties mailProperties;

    @Override
    public void run(ApplicationArguments args) {
        if (!Arrays.asList(environment.getActiveProfiles()).contains("prod")) {
            return;
        }

        require("JWT_SECRET", jwtConfig.getSecret());
        if (jwtConfig.getSecret().length() < 64) {
            throw new IllegalStateException("JWT_SECRET must be at least 64 characters in production");
        }

        require("SPRING_DATASOURCE_URL", environment.getProperty("spring.datasource.url"));
        require("SPRING_DATASOURCE_USERNAME", environment.getProperty("spring.datasource.username"));
        require("SPRING_DATASOURCE_PASSWORD", environment.getProperty("spring.datasource.password"));

        require("SPRING_MAIL_HOST", environment.getProperty("spring.mail.host"));
        require("SPRING_MAIL_USERNAME", environment.getProperty("spring.mail.username"));
        require("SPRING_MAIL_PASSWORD", environment.getProperty("spring.mail.password"));
        rejectPlaceholder("SPRING_MAIL_USERNAME", environment.getProperty("spring.mail.username"));
        rejectPlaceholder("SPRING_MAIL_PASSWORD", environment.getProperty("spring.mail.password"));
        requireEmail("APP_MAIL_FROM", mailProperties.getFrom());
        requireUrl("APP_MAIL_FRONTEND_BASE_URL", mailProperties.getFrontendBaseUrl());
        requireHttpsUrl("APP_MAIL_VERIFICATION_BASE_URL", mailProperties.getVerificationBaseUrl());

        require("CLOUDINARY_CLOUD_NAME", environment.getProperty("app.cloudinary.cloud-name"));
        require("CLOUDINARY_API_KEY", environment.getProperty("app.cloudinary.api-key"));
        require("CLOUDINARY_API_SECRET", environment.getProperty("app.cloudinary.api-secret"));
    }

    private void require(String name, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " must be configured for the prod profile");
        }
    }

    private void requireEmail(String name, String value) {
        require(name, value);
        if (!EMAIL_PATTERN.matcher(value).matches() || isPlaceholder(value)) {
            throw new IllegalStateException(name + " must be a valid email address for the prod profile");
        }
    }

    private void requireUrl(String name, String value) {
        require(name, value);
        try {
            URI uri = new URI(value);
            if (uri.getScheme() == null || uri.getHost() == null || isPlaceholder(value)) {
                throw new IllegalStateException(name + " must be a valid URL for the prod profile");
            }
        } catch (URISyntaxException ex) {
            throw new IllegalStateException(name + " must be a valid URL for the prod profile");
        }
    }

    private void requireHttpsUrl(String name, String value) {
        requireUrl(name, value);
        try {
            URI uri = new URI(value);
            if (!"https".equalsIgnoreCase(uri.getScheme())) {
                throw new IllegalStateException(name + " must be a valid HTTPS URL for the prod profile");
            }
        } catch (URISyntaxException ex) {
            throw new IllegalStateException(name + " must be a valid HTTPS URL for the prod profile");
        }
    }

    private void rejectPlaceholder(String name, String value) {
        require(name, value);
        if (isPlaceholder(value)) {
            throw new IllegalStateException(name + " must not use placeholder credentials in production");
        }
    }

    private boolean isPlaceholder(String value) {
        String normalized = value == null ? "" : value.toLowerCase(Locale.ROOT);
        return normalized.contains("change-this")
                || normalized.contains("example")
                || normalized.contains("placeholder")
                || normalized.contains("your-")
                || normalized.equals("password")
                || normalized.equals("secret");
    }
}
