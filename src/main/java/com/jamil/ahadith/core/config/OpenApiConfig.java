package com.jamil.ahadith.core.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
    private static final String BEARER_AUTH = "bearer-jwt";

    @Bean
    public OpenAPI ahadithOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Ahadith API")
                        .version("v1")
                        .description("API for Ahadith authentication, public catalog/search, member workflows, scholar workflows, and administration."))
                .servers(List.of(
                        new Server().url("https://api.jamilhelal.me").description("Production"),
                        new Server().url("http://localhost:8080").description("Local")
                ))
                .components(new Components().addSecuritySchemes(
                        BEARER_AUTH,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                ))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("Public API")
                .pathsToMatch("/api/v1/auth/**", "/api/v1/ahadith/**", "/api/v1/books/**",
                        "/api/v1/rawis/**", "/api/v1/rulings/**", "/api/v1/topics/**",
                        "/api/v1/muhaddiths/**", "/api/v1/search/filters")
                .build();
    }

    @Bean
    public GroupedOpenApi memberApi() {
        return GroupedOpenApi.builder()
                .group("Member API")
                .pathsToMatch("/api/v1/me/**")
                .build();
    }

    @Bean
    public GroupedOpenApi scholarApi() {
        return GroupedOpenApi.builder()
                .group("Scholar API")
                .pathsToMatch("/api/v1/scholar/**")
                .build();
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("Admin API")
                .pathsToMatch("/api/v1/admin/**")
                .build();
    }
}
