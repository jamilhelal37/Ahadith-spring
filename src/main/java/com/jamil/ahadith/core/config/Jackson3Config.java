package com.jamil.ahadith.core.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.module.SimpleModule;

import java.time.LocalDate;

@Configuration
public class Jackson3Config {

    @Bean
    public JsonMapperBuilderCustomizer flexibleLocalDateJackson3Customizer() {
        return builder -> {
            SimpleModule module = new SimpleModule("FlexibleLocalDate");
            module.addDeserializer(LocalDate.class, new FlexibleLocalDateJackson3Deserializer());
            builder.addModule(module);
        };
    }
}
