package com.jamil.ahadith.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {
    private int searchHistoryMaxPerUser = 50;
    private int loginMaxFailures = 5;
    private Duration loginLockDuration = Duration.ofMinutes(15);
    private boolean trustedProxyHeaders = false;
}
