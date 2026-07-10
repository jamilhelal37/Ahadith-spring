package com.jamil.ahadith.services;

import com.jamil.ahadith.config.MailConfigProperties;
import com.jamil.ahadith.entities.User;
import com.jamil.ahadith.exceptions.EmailDeliveryException;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@Profile("!test")
@ConditionalOnProperty(prefix = "app.mail", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class SmtpEmailService implements EmailService {
    private final JavaMailSender mailSender;
    private final MailConfigProperties mailProperties;

    @Override
    public void sendVerificationEmail(User user, String token) {
        send(user.getEmail(), "Verify your Ahadith account",
                "تأكيد حسابك في Ahadith\n\nرابط تأكيد البريد الإلكتروني:\n"
                        + buildLink(mailProperties.getVerificationBaseUrl(), mailProperties.getVerificationPath(), token));
    }

    @Override
    public void sendPasswordResetEmail(User user, String token) {
        send(user.getEmail(), "Reset your Ahadith password",
                "إعادة تعيين كلمة مرور Ahadith\n\nReset your password:\n"
                        + buildLink(mailProperties.getFrontendBaseUrl(), "/reset-password", token));
    }

    @PostConstruct
    void validateMailUrls() {
        requireAbsoluteUrl("app.mail.verification-base-url", mailProperties.getVerificationBaseUrl());
        requireAbsoluteUrl("app.mail.frontend-base-url", mailProperties.getFrontendBaseUrl());
    }

    private void send(String to, String subject, String text) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
            helper.setFrom(mailProperties.getFrom());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, false);
            mailSender.send(message);
        } catch (MailException | MessagingException ex) {
            throw new EmailDeliveryException("Email delivery failed", ex);
        }
    }

    String buildVerificationLinkForTesting(String token) {
        return buildLink(mailProperties.getVerificationBaseUrl(), mailProperties.getVerificationPath(), token);
    }

    String buildPasswordResetLinkForTesting(String token) {
        return buildLink(mailProperties.getFrontendBaseUrl(), "/reset-password", token);
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
            throw new IllegalStateException(propertyName + " must be configured when SMTP email is enabled");
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
