package com.jamil.ahadith;

import com.jamil.ahadith.config.MailConfigProperties;
import com.jamil.ahadith.exceptions.EmailDeliveryException;
import com.jamil.ahadith.services.EmailService;
import com.jamil.ahadith.services.NoOpEmailService;
import com.jamil.ahadith.services.SmtpEmailService;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MailConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MailSenderAutoConfiguration.class))
            .withUserConfiguration(MailBeans.class);

    @Test
    void javaMailSenderAndSmtpEmailServiceShouldBeCreatedWhenMailIsEnabled() {
        contextRunner
                .withPropertyValues(
                        "app.mail.enabled=true",
                        "app.mail.from=sender@example.com",
                        "app.mail.frontend-base-url=https://app.example.com",
                        "app.mail.verification-base-url=http://localhost:8080",
                        "app.mail.verification-path=/verify-email",
                        "spring.mail.host=smtp.gmail.com",
                        "spring.mail.port=587",
                        "spring.mail.username=sender@example.com",
                        "spring.mail.password=test-app-password")
                .run(context -> {
                    assertThat(context).hasSingleBean(JavaMailSender.class);
                    assertThat(context).hasSingleBean(EmailService.class);
                    assertThat(context.getBean(EmailService.class)).isInstanceOf(SmtpEmailService.class);
                });
    }

    @Test
    void noOpEmailServiceShouldBeCreatedWhenMailIsDisabledWithoutGmailCredentials() {
        new ApplicationContextRunner()
                .withUserConfiguration(MailBeans.class)
                .withPropertyValues("app.mail.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(JavaMailSender.class);
                    assertThat(context).hasSingleBean(EmailService.class);
                    assertThat(context.getBean(EmailService.class)).isInstanceOf(NoOpEmailService.class);
                });
    }

    @Test
    void verificationEmailShouldContainRecipientFromSubjectUtf8AndLocalRawTokenLink() throws Exception {
        JavaMailSender sender = mock(JavaMailSender.class);
        MimeMessage message = mimeMessage();
        when(sender.createMimeMessage()).thenReturn(message);
        SmtpEmailService service = new SmtpEmailService(
                sender, mailProperties("https://frontend.example.com/", "http://localhost:8080/"));

        service.sendVerificationEmail(user("recipient@example.com"), "raw-token-123");

        verify(sender).send(message);
        assertThat(message.getFrom()[0].toString()).isEqualTo("sender@example.com");
        assertThat(message.getRecipients(Message.RecipientType.TO)[0].toString()).isEqualTo("recipient@example.com");
        assertThat(message.getSubject()).isEqualTo("Verify your Ahadith account");
        assertThat(message.getContent().toString()).contains("تأكيد حسابك");
        assertThat(message.getContent().toString())
                .contains("http://localhost:8080/verify-email?token=raw-token-123")
                .doesNotContain("//verify-email");
    }

    @Test
    void renderVerificationUrlShouldUseRenderExternalUrlWhenConfigured() throws Exception {
        JavaMailSender sender = mock(JavaMailSender.class);
        MimeMessage message = mimeMessage();
        when(sender.createMimeMessage()).thenReturn(message);
        SmtpEmailService service = new SmtpEmailService(
                sender, mailProperties("https://frontend.example.com", "https://example-api.onrender.com"));

        service.sendVerificationEmail(user("recipient@example.com"), "render-token");

        assertThat(message.getContent().toString())
                .contains("https://example-api.onrender.com/verify-email?token=render-token");
    }

    @Test
    void explicitVerificationBaseUrlShouldOverrideFrontendBaseUrl() throws Exception {
        JavaMailSender sender = mock(JavaMailSender.class);
        MimeMessage message = mimeMessage();
        when(sender.createMimeMessage()).thenReturn(message);
        SmtpEmailService service = new SmtpEmailService(
                sender, mailProperties("https://frontend.example.com", "https://custom.example.com"));

        service.sendVerificationEmail(user("recipient@example.com"), "custom-token");

        assertThat(message.getContent().toString())
                .contains("https://custom.example.com/verify-email?token=custom-token")
                .doesNotContain("https://frontend.example.com/verify-email");
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
    void verificationUrlShouldEncodeSensitiveCharactersAndAvoidDoubleSlash() throws Exception {
        JavaMailSender sender = mock(JavaMailSender.class);
        MimeMessage message = mimeMessage();
        when(sender.createMimeMessage()).thenReturn(message);
        SmtpEmailService service = new SmtpEmailService(
                sender, mailProperties("https://frontend.example.com", "https://example-api.onrender.com/"));

        service.sendVerificationEmail(user("recipient@example.com"), "raw token+/=");

        assertThat(message.getContent().toString())
                .contains("https://example-api.onrender.com/verify-email?token=raw%20token%2B%2F%3D")
                .doesNotContain("//verify-email");
    }

    @Test
    void resetEmailShouldKeepUsingFrontendBaseUrl() throws Exception {
        JavaMailSender sender = mock(JavaMailSender.class);
        MimeMessage message = mimeMessage();
        when(sender.createMimeMessage()).thenReturn(message);
        SmtpEmailService service = new SmtpEmailService(
                sender, mailProperties("https://frontend.example.com", "https://api.example.com"));

        service.sendPasswordResetEmail(user("recipient@example.com"), "reset-token-123");

        verify(sender).send(message);
        assertThat(message.getFrom()[0].toString()).isEqualTo("sender@example.com");
        assertThat(message.getRecipients(Message.RecipientType.TO)[0].toString()).isEqualTo("recipient@example.com");
        assertThat(message.getSubject()).isEqualTo("Reset your Ahadith password");
        assertThat(message.getContent().toString()).contains("إعادة تعيين");
        assertThat(message.getContent().toString())
                .contains("https://frontend.example.com/reset-password?token=reset-token-123")
                .doesNotContain("//reset-password");
    }

    @Test
    void smtpFailureShouldThrowSafeException() {
        JavaMailSender sender = mock(JavaMailSender.class);
        when(sender.createMimeMessage()).thenReturn(mimeMessage());
        doThrow(new MailSendException("authentication failed for hidden credentials"))
                .when(sender).send(any(MimeMessage.class));
        SmtpEmailService service = new SmtpEmailService(sender, mailProperties("https://frontend.example.com"));

        assertThatThrownBy(() -> service.sendVerificationEmail(user("recipient@example.com"), "raw-token-123"))
                .isInstanceOf(EmailDeliveryException.class)
                .hasMessage("Email delivery failed")
                .hasMessageNotContaining("credentials")
                .hasMessageNotContaining("raw-token-123");
    }

    private MimeMessage mimeMessage() {
        return new MimeMessage(Session.getInstance(new Properties()));
    }

    private MailConfigProperties mailProperties(String frontendBaseUrl) {
        return mailProperties(frontendBaseUrl, frontendBaseUrl);
    }

    private MailConfigProperties mailProperties(String frontendBaseUrl, String verificationBaseUrl) {
        MailConfigProperties properties = new MailConfigProperties();
        properties.setEnabled(true);
        properties.setFrom("sender@example.com");
        properties.setFrontendBaseUrl(frontendBaseUrl);
        properties.setVerificationBaseUrl(verificationBaseUrl);
        properties.setVerificationPath("/verify-email");
        return properties;
    }

    private com.jamil.ahadith.entities.User user(String email) {
        com.jamil.ahadith.entities.User user = new com.jamil.ahadith.entities.User();
        user.setEmail(email);
        return user;
    }

    @Configuration
    @EnableConfigurationProperties(MailConfigProperties.class)
    @Import({SmtpEmailService.class, NoOpEmailService.class})
    static class MailBeans {
    }

    @Configuration
    @EnableConfigurationProperties(MailConfigProperties.class)
    static class MailPropertiesOnly {
    }
}
