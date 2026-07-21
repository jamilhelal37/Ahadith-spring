package com.jamil.ahadith.core.mail;

import com.jamil.ahadith.core.config.MailConfigProperties;
import com.jamil.ahadith.core.exception.EmailDeliveryException;
import com.jamil.ahadith.features.user.entity.User;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Service
@Profile("!test")
@ConditionalOnProperty(prefix = "app.mail", name = "enabled", havingValue = "true")
@ConditionalOnExpression("'${app.mail.provider:resend}'.equalsIgnoreCase('resend')")
@RequiredArgsConstructor
public class ResendEmailService implements EmailService {
    private static final String RESEND_EMAILS_URL = "https://api.resend.com/emails";

    private final RestClient.Builder restClientBuilder;
    private final MailConfigProperties mailProperties;
    private final EmailLinkBuilder emailLinkBuilder;

    private RestClient restClient;

    @PostConstruct
    void initialize() {
        emailLinkBuilder.validateConfiguredUrls();
        restClient = restClientBuilder.build();
    }

    @Override
    public void sendVerificationEmail(User user, String token) {
        send(user.getEmail(), "Verify your Ahadith account",
                "ØªØ£ÙƒÙŠØ¯ Ø­Ø³Ø§Ø¨Ùƒ ÙÙŠ Ahadith\n\nØ±Ø§Ø¨Ø· ØªØ£ÙƒÙŠØ¯ Ø§Ù„Ø¨Ø±ÙŠØ¯ Ø§Ù„Ø¥Ù„ÙƒØªØ±ÙˆÙ†ÙŠ:\n"
                        + emailLinkBuilder.verificationLink(token));
    }

    @Override
    public void sendPasswordResetEmail(User user, String token) {
        send(user.getEmail(), "Reset your Ahadith password",
                "Ø¥Ø¹Ø§Ø¯Ø© ØªØ¹ÙŠÙŠÙ† ÙƒÙ„Ù…Ø© Ù…Ø±ÙˆØ± Ahadith\n\nReset your password:\n"
                        + emailLinkBuilder.passwordResetLink(token));
    }

    private void send(String to, String subject, String text) {
        try {
            restClient.post()
                    .uri(RESEND_EMAILS_URL)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + mailProperties.getResendApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ResendEmailRequest(mailProperties.getFrom(), List.of(to), subject, text))
                    .retrieve()
                    .onStatus(status -> !status.is2xxSuccessful(), (request, response) -> {
                        throw new EmailDeliveryException("Email delivery failed", null);
                    })
                    .toBodilessEntity();
        } catch (EmailDeliveryException ex) {
            throw ex;
        } catch (RestClientException ex) {
            throw new EmailDeliveryException("Email delivery failed", ex);
        }
    }

    private record ResendEmailRequest(
            String from,
            List<String> to,
            String subject,
            String text
    ) {
    }
}
