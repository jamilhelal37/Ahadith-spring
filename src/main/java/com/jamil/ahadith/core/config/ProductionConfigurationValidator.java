package com.jamil.ahadith.core.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;

@Component
public class ProductionConfigurationValidator implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(ProductionConfigurationValidator.class);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final Environment environment;
    private final JwtConfig jwtConfig;
    private final MailConfigProperties mailProperties;
    private final SecurityProperties securityProperties;

    @Autowired
    public ProductionConfigurationValidator(
            Environment environment,
            JwtConfig jwtConfig,
            MailConfigProperties mailProperties,
            SecurityProperties securityProperties
    ) {
        this.environment = environment;
        this.jwtConfig = jwtConfig;
        this.mailProperties = mailProperties;
        this.securityProperties = securityProperties;
    }

    public ProductionConfigurationValidator(
            Environment environment,
            JwtConfig jwtConfig,
            MailConfigProperties mailProperties
    ) {
        this(environment, jwtConfig, mailProperties, new SecurityProperties());
    }

    @Override
    public void run(ApplicationArguments args) {
        validateJwtSecret();

        if (!Arrays.asList(environment.getActiveProfiles()).contains("prod")) {
            return;
        }

        require("SPRING_DATASOURCE_URL", environment.getProperty("spring.datasource.url"));
        require("SPRING_DATASOURCE_USERNAME", environment.getProperty("spring.datasource.username"));
        require("SPRING_DATASOURCE_PASSWORD", environment.getProperty("spring.datasource.password"));

        validateMailConfiguration();
        warnIfProxyHeadersDisabled();

        require("CLOUDINARY_CLOUD_NAME", environment.getProperty("app.cloudinary.cloud-name"));
        require("CLOUDINARY_API_KEY", environment.getProperty("app.cloudinary.api-key"));
        require("CLOUDINARY_API_SECRET", environment.getProperty("app.cloudinary.api-secret"));
    }

    private void validateJwtSecret() {
        String secret = jwtConfig.getSecret();
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET must be configured");
        }
        if (secret.length() < 64) {
            throw new IllegalStateException("JWT_SECRET must be at least 64 characters");
        }
    }

    private void validateMailConfiguration() {
        String provider = normalizedMailProvider();
        if (!"resend".equals(provider)) {
            throw new IllegalStateException("APP_MAIL_PROVIDER must be one of: resend");
        }
        if (!mailProperties.isEnabled()) {
            return;
        }

        require("RESEND_API_KEY", mailProperties.getResendApiKey());
        rejectPlaceholder("RESEND_API_KEY", mailProperties.getResendApiKey());
        requireEmail("APP_MAIL_FROM", mailProperties.getFrom());
        requireHttpsUrl("APP_MAIL_FRONTEND_BASE_URL", mailProperties.getFrontendBaseUrl());
        requireHttpsUrl("APP_MAIL_VERIFICATION_BASE_URL", mailProperties.getVerificationBaseUrl());
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

    private String normalizedMailProvider() {
        String provider = mailProperties.getProvider();
        if (provider == null || provider.isBlank()) {
            return "resend";
        }
        return provider.trim().toLowerCase(Locale.ROOT);
    }

    private void warnIfProxyHeadersDisabled() {
        if (!securityProperties.isTrustedProxyHeaders()) {
            log.warn("APP_SECURITY_TRUSTED_PROXY_HEADERS is false while prod profile is active; client IP resolution will ignore proxy headers.");
        }
    }
}
