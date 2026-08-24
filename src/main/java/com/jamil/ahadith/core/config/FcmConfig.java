package com.jamil.ahadith.core.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.jamil.ahadith.features.notification.fcm.FcmSender;
import com.jamil.ahadith.features.notification.fcm.FirebaseAdminFcmSender;
import com.jamil.ahadith.features.notification.fcm.NoOpFcmSender;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
public class FcmConfig {
    private static final String FIREBASE_APP_NAME = "ahadith-fcm";
    private static final String GOOGLE_APPLICATION_CREDENTIALS = "GOOGLE_APPLICATION_CREDENTIALS";

    @Bean
    @ConditionalOnProperty(prefix = "app.fcm", name = "enabled", havingValue = "true")
    public FirebaseApp firebaseApp(Environment environment) {
        String credentialsPath = environment.getProperty(GOOGLE_APPLICATION_CREDENTIALS);
        if (credentialsPath == null || credentialsPath.isBlank()) {
            throw new IllegalStateException(
                    "GOOGLE_APPLICATION_CREDENTIALS must point to a Firebase service account JSON file when APP_FCM_ENABLED=true"
            );
        }

        Path path = Path.of(credentialsPath.trim());
        if (!Files.isRegularFile(path)) {
            throw new IllegalStateException(
                    "GOOGLE_APPLICATION_CREDENTIALS must point to a readable Firebase service account JSON file"
            );
        }

        try {
            return existingFirebaseAppOrInitialize(path);
        } catch (IOException ex) {
            throw new IllegalStateException(
                    "Firebase credentials could not be loaded from GOOGLE_APPLICATION_CREDENTIALS",
                    ex
            );
        }
    }

    @Bean
    @ConditionalOnBean(FirebaseApp.class)
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }

    @Bean
    @ConditionalOnBean(FirebaseMessaging.class)
    public FcmSender firebaseAdminFcmSender(FirebaseMessaging firebaseMessaging) {
        return new FirebaseAdminFcmSender(firebaseMessaging);
    }

    @Bean
    @ConditionalOnMissingBean(FcmSender.class)
    public FcmSender noOpFcmSender() {
        return new NoOpFcmSender();
    }

    private FirebaseApp existingFirebaseAppOrInitialize(Path credentialsPath) throws IOException {
        for (FirebaseApp app : FirebaseApp.getApps()) {
            if (FIREBASE_APP_NAME.equals(app.getName())) {
                return app;
            }
        }

        try (InputStream credentials = Files.newInputStream(credentialsPath)) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(credentials))
                    .build();
            return FirebaseApp.initializeApp(options, FIREBASE_APP_NAME);
        }
    }
}
