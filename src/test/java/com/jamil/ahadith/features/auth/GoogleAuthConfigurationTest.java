package com.jamil.ahadith.features.auth;

import com.jamil.ahadith.core.config.GoogleAuthProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class GoogleAuthConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
            .withUserConfiguration(GoogleAuthPropertiesTestConfig.class);

    @Test
    void applicationShouldStartWhenGoogleAuthIsDisabledWithoutClientIds() {
        contextRunner
                .withPropertyValues("app.google-auth.enabled=false")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context.getBean(GoogleAuthProperties.class).audiences()).isEmpty();
                });
    }

    @Test
    void applicationShouldFailClearlyWhenGoogleAuthEnabledWithoutClientIds() {
        contextRunner
                .withPropertyValues("app.google-auth.enabled=true")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasStackTraceContaining("GOOGLE_AUTH_CLIENT_IDS must be configured");
                });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(GoogleAuthProperties.class)
    static class GoogleAuthPropertiesTestConfig {
    }
}
