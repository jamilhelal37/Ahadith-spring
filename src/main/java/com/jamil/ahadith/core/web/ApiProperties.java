package com.jamil.ahadith.core.web;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.api")
public class ApiProperties {
    private String legacySunset;
}
