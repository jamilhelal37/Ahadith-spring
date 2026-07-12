package com.jamil.ahadith;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
class PostgresFlywayMigrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("ahadith_test")
            .withUsername("test")
            .withPassword("test");

    @Test
    void flywayMigrationsShouldApplyOnPostgreSql16WithSearchObjects() {
        DataSource dataSource = dataSource();

        Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .cleanDisabled(true)
                .load()
                .migrate();

        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        assertThat(jdbc.queryForObject(
                "select count(*) from flyway_schema_history where success = true", Integer.class))
                .isGreaterThanOrEqualTo(7);
        assertThat(jdbc.queryForObject(
                "select count(*) from pg_extension where extname in ('pg_trgm', 'pgcrypto')", Integer.class))
                .isEqualTo(2);
        assertThat(jdbc.queryForObject(
                "select count(*) from pg_proc p join pg_namespace n on n.oid = p.pronamespace " +
                        "where n.nspname = 'public' and p.proname in ('arab_norm', 'set_ahadith_search_vector')",
                Integer.class))
                .isEqualTo(2);
        assertThat(jdbc.queryForObject(
                "select count(*) from pg_trigger where tgname in ('trg_ahadith_search_vector', 'trg_users_updated_at')",
                Integer.class))
                .isEqualTo(2);
        assertThat(jdbc.queryForObject(
                "select count(*) from pg_indexes where schemaname = 'public' and indexname = 'uq_users_email_lower'",
                Integer.class))
                .isEqualTo(1);
    }

    private DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(POSTGRES.getDriverClassName());
        dataSource.setUrl(POSTGRES.getJdbcUrl());
        dataSource.setUsername(POSTGRES.getUsername());
        dataSource.setPassword(POSTGRES.getPassword());
        return dataSource;
    }
}
