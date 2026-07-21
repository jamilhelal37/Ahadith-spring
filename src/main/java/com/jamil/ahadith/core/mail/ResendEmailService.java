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
    private static final String SENDER_DISPLAY_NAME = "موسوعة الأحاديث النبوية";
    private static final String VERIFICATION_SUBJECT = "تأكيد بريدك الإلكتروني | موسوعة الأحاديث النبوية";
    private static final String PASSWORD_RESET_SUBJECT = "إعادة تعيين كلمة المرور | موسوعة الأحاديث النبوية";

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
        String verificationLink = emailLinkBuilder.verificationLink(token);
        send(user.getEmail(), VERIFICATION_SUBJECT,
                verificationEmailHtml(verificationLink),
                verificationEmailText(verificationLink));
    }

    @Override
    public void sendPasswordResetEmail(User user, String token) {
        String resetLink = emailLinkBuilder.passwordResetLink(token);
        send(user.getEmail(), PASSWORD_RESET_SUBJECT,
                passwordResetEmailHtml(resetLink),
                passwordResetEmailText(resetLink));
    }

    private void send(String to, String subject, String html, String text) {
        try {
            restClient.post()
                    .uri(RESEND_EMAILS_URL)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + mailProperties.getResendApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ResendEmailRequest(formattedFrom(), List.of(to), subject, html, text))
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

    private String formattedFrom() {
        return SENDER_DISPLAY_NAME + " <" + mailProperties.getFrom() + ">";
    }

    private String verificationEmailText(String verificationLink) {
        return """
                مرحباً بك في موسوعة الأحاديث النبوية

                لتفعيل حسابك، استخدم الرابط التالي:
                %s

                إذا لم تقم بإنشاء هذا الحساب، يمكنك تجاهل هذه الرسالة.
                صلاحية رابط التحقق محدودة، ولا يجوز مشاركته مع أي شخص.
                """.formatted(verificationLink);
    }

    private String passwordResetEmailText(String resetLink) {
        return """
                مرحباً بك في موسوعة الأحاديث النبوية

                لإعادة تعيين كلمة المرور، استخدم الرابط التالي:
                %s

                إذا لم تطلب إعادة تعيين كلمة المرور، يمكنك تجاهل هذه الرسالة.
                صلاحية رابط إعادة التعيين محدودة، ولا يجوز مشاركته مع أي شخص.
                """.formatted(resetLink);
    }

    private String verificationEmailHtml(String verificationLink) {
        return emailHtml(
                "تأكيد البريد الإلكتروني",
                "مرحباً بك في موسوعة الأحاديث النبوية.",
                "اضغط على الزر التالي لتفعيل حسابك:",
                "تأكيد الحساب",
                verificationLink,
                "إذا لم تقم بإنشاء هذا الحساب، يمكنك تجاهل هذه الرسالة.",
                "صلاحية رابط التحقق محدودة، ولا يجوز مشاركته مع أي شخص.");
    }

    private String passwordResetEmailHtml(String resetLink) {
        return emailHtml(
                "إعادة تعيين كلمة المرور",
                "مرحباً بك في موسوعة الأحاديث النبوية.",
                "اضغط على الزر التالي لإعادة تعيين كلمة المرور:",
                "إعادة تعيين كلمة المرور",
                resetLink,
                "إذا لم تطلب إعادة تعيين كلمة المرور، يمكنك تجاهل هذه الرسالة.",
                "صلاحية رابط إعادة التعيين محدودة، ولا يجوز مشاركته مع أي شخص.");
    }

    private String emailHtml(
            String title,
            String greeting,
            String instruction,
            String buttonText,
            String link,
            String ignoreMessage,
            String expiryMessage
    ) {
        String escapedLink = escapeHtml(link);
        return """
                <!doctype html>
                <html lang="ar" dir="rtl">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                </head>
                <body style="margin:0;background:#f4f6f8;font-family:Tahoma,Arial,sans-serif;color:#1f2937">
                    <div style="max-width:600px;margin:30px auto;background:#ffffff;padding:32px;border-radius:12px;box-sizing:border-box">
                        <h1 style="margin:0 0 20px;color:#1f2937;font-size:26px;line-height:1.4">%s</h1>
                        <p style="margin:0 0 16px;font-size:16px;line-height:1.8">%s</p>
                        <p style="margin:0 0 24px;font-size:16px;line-height:1.8">%s</p>

                        <a href="%s"
                           style="display:inline-block;background:#198754;color:#ffffff;padding:12px 24px;border-radius:8px;text-decoration:none;font-weight:bold;font-size:16px">
                            %s
                        </a>

                        <p style="margin:24px 0 8px;font-size:14px;line-height:1.8">إذا لم يعمل الزر، انسخ الرابط التالي:</p>
                        <p style="direction:ltr;text-align:left;word-break:break-all;margin:0 0 24px;font-size:14px;line-height:1.7;color:#374151">
                            %s
                        </p>

                        <p style="margin:0 0 8px;color:#6b7280;font-size:14px;line-height:1.8">
                            %s
                        </p>
                        <p style="margin:0;color:#6b7280;font-size:14px;line-height:1.8">
                            %s
                        </p>
                    </div>
                </body>
                </html>
                """.formatted(title, greeting, instruction, escapedLink, buttonText, escapedLink, ignoreMessage, expiryMessage);
    }

    private String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private record ResendEmailRequest(
            String from,
            List<String> to,
            String subject,
            String html,
            String text
    ) {
    }
}
