package com.jamil.ahadith.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LegacyJackson2Config {

    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper legacyJackson2ObjectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }
}
