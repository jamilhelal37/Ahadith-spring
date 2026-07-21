package com.jamil.ahadith.core.mail;

import com.jamil.ahadith.core.config.MailConfigProperties;
import com.jamil.ahadith.core.exception.EmailDeliveryException;
import com.jamil.ahadith.features.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.test.web.client.MockRestServiceServer;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class MailConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RestClientAutoConfiguration.class))
            .withUserConfiguration(MailBeans.class);

    @Test
    void resendEmailServiceShouldBeCreatedWhenMailIsEnabledWithResendProvider() {
        contextRunner
                .withPropertyValues(
                        "app.mail.enabled=true",
                        "app.mail.provider=resend",
                        "app.mail.resend-api-key=test-resend-key",
                        "app.mail.from=sender@example.com",
                        "app.mail.frontend-base-url=https://app.example.com",
                        "app.mail.verification-base-url=http://localhost:8080",
                        "app.mail.verification-path=/verify-email")
                .run(context -> {
                    assertThat(context).hasSingleBean(EmailService.class);
                    assertThat(context.getBean(EmailService.class)).isInstanceOf(ResendEmailService.class);
                    assertThat(context).doesNotHaveBean("mailSender");
                    assertThat(context).doesNotHaveBean("mailHealthContributor");
                });
    }

    @Test
    void noOpEmailServiceShouldBeCreatedWhenMailIsDisabled() {
        new ApplicationContextRunner()
                .withUserConfiguration(MailBeans.class)
                .withPropertyValues("app.mail.enabled=false")
                .run(context -> {
                    assertThat(context).hasSingleBean(EmailService.class);
                    assertThat(context.getBean(EmailService.class)).isInstanceOf(NoOpEmailService.class);
                    assertThat(context).doesNotHaveBean("mailSender");
                    assertThat(context).doesNotHaveBean("mailHealthContributor");
                });
    }

    @Test
    void resendVerificationEmailShouldSendExpectedHttpRequest() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        ResendEmailService service = new ResendEmailService(
                builder,
                mailProperties("https://frontend.example.com/", "http://localhost:8080/"),
                new EmailLinkBuilder(mailProperties("https://frontend.example.com/", "http://localhost:8080/")));
        service.initialize();

        server.expect(once(), requestTo("https://api.resend.com/emails"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer test-resend-key"))
                .andExpect(header("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().json("""
                        {
                          "from": "sender@example.com",
                          "to": ["recipient@example.com"],
                          "subject": "Verify your Ahadith account",
                          "text": "ØªØ£ÙƒÙŠØ¯ Ø­Ø³Ø§Ø¨Ùƒ ÙÙŠ Ahadith\\n\\nØ±Ø§Ø¨Ø· ØªØ£ÙƒÙŠØ¯ Ø§Ù„Ø¨Ø±ÙŠØ¯ Ø§Ù„Ø¥Ù„ÙƒØªØ±ÙˆÙ†ÙŠ:\\nhttp://localhost:8080/verify-email?token=raw-token-123"
                        }
                        """))
                .andRespond(withSuccess("{\"id\":\"email-id\"}", MediaType.APPLICATION_JSON));

        service.sendVerificationEmail(user("recipient@example.com"), "raw-token-123");

        server.verify();
    }

    @Test
    void resendShouldTreatSuccessfulResponseAsSent() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        ResendEmailService service = resendService(builder, mailProperties("https://frontend.example.com"));

        server.expect(once(), requestTo("https://api.resend.com/emails"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        service.sendVerificationEmail(user("recipient@example.com"), "success-token");

        server.verify();
    }

    @Test
    void resendHttpFailuresShouldThrowSafeEmailDeliveryException() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        ResendEmailService service = resendService(builder, mailProperties("https://frontend.example.com"));

        server.expect(once(), requestTo("https://api.resend.com/emails"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST).body("invalid api key or raw-token-123"));

        assertThatThrownBy(() -> service.sendVerificationEmail(user("recipient@example.com"), "raw-token-123"))
                .isInstanceOf(EmailDeliveryException.class)
                .hasMessage("Email delivery failed")
                .hasMessageNotContaining("raw-token-123")
                .hasMessageNotContaining("test-resend-key");

        server.verify();
    }

    @Test
    void resendServerFailuresShouldThrowSafeEmailDeliveryException() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        ResendEmailService service = resendService(builder, mailProperties("https://frontend.example.com"));

        server.expect(once(), requestTo("https://api.resend.com/emails"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> service.sendPasswordResetEmail(user("recipient@example.com"), "reset-token"))
                .isInstanceOf(EmailDeliveryException.class)
                .hasMessage("Email delivery failed")
                .hasMessageNotContaining("reset-token")
                .hasMessageNotContaining("test-resend-key");

        server.verify();
    }

    @Test
    void resendRedirectShouldThrowSafeEmailDeliveryException() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        ResendEmailService service = resendService(builder, mailProperties("https://frontend.example.com"));

        server.expect(once(), requestTo("https://api.resend.com/emails"))
                .andRespond(withStatus(HttpStatus.FOUND));

        assertThatThrownBy(() -> service.sendVerificationEmail(user("recipient@example.com"), "redirect-token"))
                .isInstanceOf(EmailDeliveryException.class)
                .hasMessage("Email delivery failed")
                .hasMessageNotContaining("redirect-token")
                .hasMessageNotContaining("test-resend-key");

        server.verify();
    }

    @Test
    void resendTimeoutShouldThrowSafeEmailDeliveryException() {
        RestClient.Builder builder = RestClient.builder()
                .requestFactory((uri, httpMethod) -> {
                    throw new ResourceAccessException("timeout while calling Resend", new IOException("timeout"));
                });
        ResendEmailService service = resendService(builder, mailProperties("https://frontend.example.com"));

        assertThatThrownBy(() -> service.sendVerificationEmail(user("recipient@example.com"), "timeout-token"))
                .isInstanceOf(EmailDeliveryException.class)
                .hasMessage("Email delivery failed")
                .hasMessageNotContaining("timeout-token")
                .hasMessageNotContaining("test-resend-key");
    }

    @Test
    void verificationBaseUrlShouldFallBackToFrontendBaseUrl() {
        new ApplicationContextRunner()
                .withUserConfiguration(MailPropertiesOnly.class)
                .withPropertyValues(
                        "APP_MAIL_FRONTEND_BASE_URL=https://example-api.example.com",
                        "app.mail.frontend-base-url=${APP_MAIL_FRONTEND_BASE_URL}",
                        "app.mail.verification-base-url=")
                .run(context -> assertThat(context.getBean(MailConfigProperties.class).getVerificationBaseUrl())
                        .isEqualTo("https://example-api.example.com"));
    }

    @Test
    void emailLinksShouldUseConfiguredBasesAndEncodeTokens() {
        EmailLinkBuilder links = new EmailLinkBuilder(
                mailProperties("https://frontend.example.com/", "https://api.jamilhelal.me/"));

        assertThat(links.verificationLink("raw token+/="))
                .isEqualTo("https://api.jamilhelal.me/verify-email?token=raw%20token%2B%2F%3D");
        assertThat(links.passwordResetLink("reset token+/="))
                .isEqualTo("https://frontend.example.com/reset-password?token=reset%20token%2B%2F%3D");
    }

    private ResendEmailService resendService(RestClient.Builder builder, MailConfigProperties properties) {
        ResendEmailService service = new ResendEmailService(builder, properties, new EmailLinkBuilder(properties));
        service.initialize();
        return service;
    }

    private MailConfigProperties mailProperties(String frontendBaseUrl) {
        return mailProperties(frontendBaseUrl, frontendBaseUrl);
    }

    private MailConfigProperties mailProperties(String frontendBaseUrl, String verificationBaseUrl) {
        MailConfigProperties properties = new MailConfigProperties();
        properties.setEnabled(true);
        properties.setProvider("resend");
        properties.setResendApiKey("test-resend-key");
        properties.setFrom("sender@example.com");
        properties.setFrontendBaseUrl(frontendBaseUrl);
        properties.setVerificationBaseUrl(verificationBaseUrl);
        properties.setVerificationPath("/verify-email");
        return properties;
    }

    private User user(String email) {
        User user = new User();
        user.setEmail(email);
        return user;
    }

    @Configuration
    @EnableConfigurationProperties(MailConfigProperties.class)
    @Import({ResendEmailService.class, NoOpEmailService.class, EmailLinkBuilder.class})
    static class MailBeans {
    }

    @Configuration
    @EnableConfigurationProperties(MailConfigProperties.class)
    static class MailPropertiesOnly {
    }
}
