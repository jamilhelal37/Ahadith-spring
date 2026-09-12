package com.jamil.ahadith.core;

import com.jamil.ahadith.features.catalog.entity.Ruling;

import com.jamil.ahadith.features.hadith.entity.Hadith;

import com.jamil.ahadith.features.hadith.entity.Explaining;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@ActiveProfiles("it")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class PostgresIntegrationTestBase {
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
            DockerImageName.parse("pgvector/pgvector:pg16").asCompatibleSubstituteFor("postgres"))
            .withDatabaseName("ahadith_it")
            .withUsername("ahadith_it")
            .withPassword("ahadith_it");

    static {
        POSTGRES.start();
    }

    @Autowired
    protected JdbcTemplate jdbc;

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.sql.init.mode", () -> "never");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @BeforeEach
    void cleanApplicationTables() {
        if (!cleanDatabaseBeforeEach()) {
            return;
        }

        jdbc.execute("""
                truncate table
                    public.user_fcm_tokens,
                    public.notifications,
                    public.activity_log,
                    public.upgrade_requests,
                    public.topic_classes,
                    public.similar_ahadith,
                    public.search_history,
                    public.questions,
                    public.favorites,
                    public.comments,
                    public.fake_ahadith,
                    public.ahadith,
                    public.topics,
                    public.books,
                    public.explaining,
                    public.rawis,
                    public.muhaddiths,
                    public.ruling,
                    public.password_reset_tokens,
                    public.email_verification_tokens,
                    public.refresh_token_sessions,
                    public.login_attempts,
                    public.users
                restart identity cascade
                """);
    }

    protected boolean cleanDatabaseBeforeEach() {
        return true;
    }
}
