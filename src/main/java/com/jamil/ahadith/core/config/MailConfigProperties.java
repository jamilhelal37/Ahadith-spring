package com.jamil.ahadith.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.mail")
public class MailConfigProperties {
    private boolean enabled = false;
    private String provider = "resend";
    private String from;
    private String frontendBaseUrl;
    private String verificationBaseUrl;
    private String verificationPath = "/verify-email";
    private String resendApiKey;
    private Duration verificationTokenTtl = Duration.ofHours(24);
    private Duration resetTokenTtl = Duration.ofMinutes(30);
    private Duration resendThrottle = Duration.ofMinutes(5);
    private Duration connectTimeout = Duration.ofSeconds(5);
    private Duration readTimeout = Duration.ofSeconds(10);

    public String getFrontendBaseUrl() {
        return frontendBaseUrl;
    }

    public String getVerificationBaseUrl() {
        return firstNonBlank(verificationBaseUrl, frontendBaseUrl);
    }

    private String firstNonBlank(String primary, String fallback) {
        return primary == null || primary.isBlank() ? fallback : primary;
    }
}
