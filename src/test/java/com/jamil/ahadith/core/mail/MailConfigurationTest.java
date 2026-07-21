package com.jamil.ahadith.core.mail;

import com.jamil.ahadith.core.config.MailConfigProperties;
import com.jamil.ahadith.core.exception.EmailDeliveryException;
import com.jamil.ahadith.features.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(OutputCaptureExtension.class)
class MailConfigurationTest {
    private static final String MOJIBAKE_O_WITH_STROKE = "\u00D8";
    private static final String MOJIBAKE_U_WITH_GRAVE = "\u00D9";
    private static final String MOJIBAKE_A_WITH_TILDE = "\u00C3";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
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

        String verificationLink = "http://localhost:8080/verify-email?token=raw-token-123";
        server.expect(once(), requestTo("https://api.resend.com/emails"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer test-resend-key"))
                .andExpect(header("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.from").value("موسوعة الأحاديث النبوية <sender@example.com>"))
                .andExpect(jsonPath("$.to[0]").value("recipient@example.com"))
                .andExpect(jsonPath("$.subject").value("تأكيد بريدك الإلكتروني | موسوعة الأحاديث النبوية"))
                .andExpect(jsonPath("$.subject").value(not(containsString(MOJIBAKE_O_WITH_STROKE))))
                .andExpect(jsonPath("$.subject").value(not(containsString(MOJIBAKE_U_WITH_GRAVE))))
                .andExpect(jsonPath("$.subject").value(not(containsString(MOJIBAKE_A_WITH_TILDE))))
                .andExpect(jsonPath("$.text").value(containsString("مرحباً بك في موسوعة الأحاديث النبوية")))
                .andExpect(jsonPath("$.text").value(containsString("لتفعيل حسابك، استخدم الرابط التالي:")))
                .andExpect(jsonPath("$.text").value(containsString(verificationLink)))
                .andExpect(jsonPath("$.text").value(not(containsString(MOJIBAKE_O_WITH_STROKE))))
                .andExpect(jsonPath("$.text").value(not(containsString(MOJIBAKE_U_WITH_GRAVE))))
                .andExpect(jsonPath("$.text").value(not(containsString(MOJIBAKE_A_WITH_TILDE))))
                .andExpect(jsonPath("$.html").value(containsString("<html lang=\"ar\" dir=\"rtl\">")))
                .andExpect(jsonPath("$.html").value(containsString("<meta charset=\"UTF-8\">")))
                .andExpect(jsonPath("$.html").value(containsString("تأكيد الحساب")))
                .andExpect(jsonPath("$.html").value(containsString(verificationLink)))
                .andExpect(jsonPath("$.html").value(not(containsString(MOJIBAKE_O_WITH_STROKE))))
                .andExpect(jsonPath("$.html").value(not(containsString(MOJIBAKE_U_WITH_GRAVE))))
                .andExpect(jsonPath("$.html").value(not(containsString(MOJIBAKE_A_WITH_TILDE))))
                .andRespond(withSuccess("{\"id\":\"email-id\"}", MediaType.APPLICATION_JSON));

        service.sendVerificationEmail(user("recipient@example.com"), "raw-token-123");

        server.verify();
    }

    @Test
    void resendPasswordResetEmailShouldSendArabicHtmlAndText() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        ResendEmailService service = resendService(builder, mailProperties("https://frontend.example.com"));

        String resetLink = "https://frontend.example.com/reset-password?token=reset-token-123";
        server.expect(once(), requestTo("https://api.resend.com/emails"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer test-resend-key"))
                .andExpect(jsonPath("$.from").value("موسوعة الأحاديث النبوية <sender@example.com>"))
                .andExpect(jsonPath("$.to[0]").value("recipient@example.com"))
                .andExpect(jsonPath("$.subject").value("إعادة تعيين كلمة المرور | موسوعة الأحاديث النبوية"))
                .andExpect(jsonPath("$.subject").value(not(containsString(MOJIBAKE_O_WITH_STROKE))))
                .andExpect(jsonPath("$.subject").value(not(containsString(MOJIBAKE_U_WITH_GRAVE))))
                .andExpect(jsonPath("$.subject").value(not(containsString(MOJIBAKE_A_WITH_TILDE))))
                .andExpect(jsonPath("$.text").value(containsString("لإعادة تعيين كلمة المرور، استخدم الرابط التالي:")))
                .andExpect(jsonPath("$.text").value(containsString(resetLink)))
                .andExpect(jsonPath("$.text").value(not(containsString(MOJIBAKE_O_WITH_STROKE))))
                .andExpect(jsonPath("$.text").value(not(containsString(MOJIBAKE_U_WITH_GRAVE))))
                .andExpect(jsonPath("$.text").value(not(containsString(MOJIBAKE_A_WITH_TILDE))))
                .andExpect(jsonPath("$.html").value(containsString("dir=\"rtl\"")))
                .andExpect(jsonPath("$.html").value(containsString("إعادة تعيين كلمة المرور")))
                .andExpect(jsonPath("$.html").value(containsString(resetLink)))
                .andExpect(jsonPath("$.html").value(not(containsString(MOJIBAKE_O_WITH_STROKE))))
                .andExpect(jsonPath("$.html").value(not(containsString(MOJIBAKE_U_WITH_GRAVE))))
                .andExpect(jsonPath("$.html").value(not(containsString(MOJIBAKE_A_WITH_TILDE))))
                .andRespond(withSuccess("{\"id\":\"email-id\"}", MediaType.APPLICATION_JSON));

        service.sendPasswordResetEmail(user("recipient@example.com"), "reset-token-123");

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
    void resendShouldNotLogApiKeyOrToken(CapturedOutput output) {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        ResendEmailService service = resendService(builder, mailProperties("https://frontend.example.com"));

        server.expect(once(), requestTo("https://api.resend.com/emails"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        service.sendVerificationEmail(user("recipient@example.com"), "secret-token-123");

        assertThat(output).doesNotContain("test-resend-key");
        assertThat(output).doesNotContain("secret-token-123");
        server.verify();
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
        @Bean
        RestClient.Builder restClientBuilder() {
            return RestClient.builder();
        }
    }

    @Configuration
    @EnableConfigurationProperties(MailConfigProperties.class)
    static class MailPropertiesOnly {
    }
}
