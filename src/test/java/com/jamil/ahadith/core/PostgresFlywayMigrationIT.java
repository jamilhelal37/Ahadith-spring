package com.jamil.ahadith.core;

import com.jamil.ahadith.features.catalog.entity.Ruling;

import com.jamil.ahadith.features.user.entity.User;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("it")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PostgresFlywayMigrationIT {
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("ahadith_migration_it")
            .withUsername("ahadith_it")
            .withPassword("ahadith_it");

    static {
        POSTGRES.start();
    }

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbc;

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

    @Test
    void flywayMigrationsShouldApplyOnPostgreSql16WithSearchObjectsAndValidatedJpaMappings() {
        MigrationInfo[] migrations = flyway.info().all();
        List<String> successfulVersions = Arrays.stream(migrations)
                .filter(migration -> migration.getState() == MigrationState.SUCCESS)
                .map(migration -> migration.getVersion().getVersion())
                .toList();

        assertThat(migrations)
                .hasSize(10)
                .allSatisfy(migration -> assertThat(migration.getState()).isNotEqualTo(MigrationState.FAILED));
        assertThat(Arrays.stream(migrations).filter(migration -> migration.getState() == MigrationState.PENDING)).isEmpty();
        assertThat(successfulVersions).containsExactly("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");

        assertThat(jdbc.queryForObject("select current_setting('server_version_num')::int", Integer.class))
                .isGreaterThanOrEqualTo(160000);
        assertThat(jdbc.queryForObject(
                "select count(*) from pg_extension where extname in ('pg_trgm', 'pgcrypto')", Integer.class))
                .isEqualTo(2);
        assertThat(jdbc.queryForObject(
                """
                select count(*)
                from pg_proc p
                join pg_namespace n on n.oid = p.pronamespace
                where n.nspname = 'public'
                  and p.proname in (
                      'arab_norm',
                      'remove_arabic_diacritics',
                      'set_arabic_text_variants',
                      'set_ahadith_search_vector',
                      'set_updated_at'
                  )
                """,
                Integer.class))
                .isEqualTo(5);
        assertThat(jdbc.queryForObject(
                """
                select count(*)
                from pg_trigger
                where not tgisinternal
                  and tgname in (
                      'trg_ahadith_text_variants',
                      'trg_ahadith_search_vector',
                      'trg_explaining_text_variants',
                      'trg_users_updated_at'
                  )
                """,
                Integer.class))
                .isEqualTo(4);
        assertThat(jdbc.queryForObject(
                """
                select count(*)
                from pg_indexes
                where schemaname = 'public'
                  and indexname in (
                      'idx_ahadith_search_vector',
                      'idx_ahadith_search_text_trgm',
                      'idx_explaining_search_text_trgm',
                      'uq_users_email_lower',
                      'idx_refresh_token_sessions_user_id',
                      'idx_refresh_token_sessions_family_id',
                      'idx_refresh_token_sessions_expires_at',
                      'idx_email_verification_tokens_user_id',
                      'idx_email_verification_tokens_expires_at',
                      'idx_password_reset_tokens_expires_at',
                      'idx_login_attempts_updated_at',
                      'idx_password_reset_tokens_expires_consumed',
                      'idx_email_verification_tokens_expires_consumed',
                      'idx_upgrade_requests_document_public_id',
                      'idx_upgrade_requests_user_created_at',
                      'idx_questions_asker_created_at',
                      'idx_comments_user_created_at',
                      'idx_search_history_user_created_at',
                      'idx_notifications_user_created_at'
                      , 'uq_users_google_subject'
                  )
                """,
                Integer.class))
                .isEqualTo(20);

        assertThat(jdbc.queryForObject(
                """
                select count(*)
                from information_schema.tables
                where table_schema = 'public'
                  and table_name in (
                      'users',
                      'refresh_token_sessions',
                      'email_verification_tokens',
                      'password_reset_tokens',
                      'login_attempts',
                      'activity_log',
                      'upgrade_requests',
                      'notifications',
                      'ahadith'
                  )
                """,
                Integer.class))
                .isEqualTo(9);
        assertThat(jdbc.queryForObject(
                """
                select count(*)
                from information_schema.columns
                where table_schema = 'public'
                  and (
                      (table_name = 'users' and column_name in ('avatar_public_id', 'token_version', 'google_subject'))
                      or (table_name = 'upgrade_requests' and column_name in (
                          'review_notes',
                          'rejection_reason',
                          'reviewed_at',
                          'document_asset_id',
                          'document_public_id',
                          'document_resource_type',
                          'document_delivery_type',
                          'document_format',
                          'document_original_name',
                          'document_size_bytes'
                      ))
                      or (table_name = 'notifications' and column_name = 'user_id')
                      or (table_name = 'refresh_token_sessions' and column_name in ('token_hash', 'token_id', 'family_id'))
                      or (table_name = 'email_verification_tokens' and column_name in ('token_hash', 'last_sent_at'))
                      or (table_name = 'password_reset_tokens' and column_name in ('token_hash', 'consumed_at'))
                      or (table_name = 'login_attempts' and column_name in ('email_key', 'ip_address', 'locked_until'))
                      or (table_name = 'activity_log' and column_name in ('actor_user_id', 'message', 'old_data', 'new_data'))
                  )
                """,
                Integer.class))
                .isEqualTo(28);
        assertThat(jdbc.queryForObject(
                """
                select is_nullable
                from information_schema.columns
                where table_schema = 'public'
                  and table_name = 'users'
                  and column_name = 'password'
                """,
                String.class))
                .isEqualTo("YES");
        assertThat(jdbc.queryForObject(
                """
                select indexdef
                from pg_indexes
                where schemaname = 'public'
                  and indexname = 'uq_users_google_subject'
                """,
                String.class))
                .contains("google_subject")
                .contains("WHERE (google_subject IS NOT NULL)");
        assertThat(jdbc.queryForObject(
                """
                select count(*)
                from pg_constraint
                where conname in (
                    'fk_notifications_user',
                    'fk_refresh_token_sessions_user',
                    'fk_email_verification_tokens_user',
                    'fk_password_reset_tokens_user',
                    'uq_login_attempts_email_ip'
                )
                """,
                Integer.class))
                .isEqualTo(5);
        assertThat(jdbc.queryForObject(
                "select count(*) from public.ruling", Integer.class))
                .isGreaterThan(0);
        assertThat(jdbc.queryForObject(
                """
                select r.name
                from public.ahadith h
                join public.books b on b.id = h.book
                join public.rawis r on r.id = h.rawi
                where b.name = 'صحيح البخاري'
                  and h.hadith_number = 6412
                """,
                String.class))
                .isEqualTo("عبد الله بن عباس");
        assertThat(jdbc.queryForObject(
                """
                select r.name
                from public.ahadith h
                join public.books b on b.id = h.book
                join public.rawis r on r.id = h.rawi
                where b.name = 'صحيح مسلم'
                  and h.hadith_number = 2999
                """,
                String.class))
                .isEqualTo("صهيب الرومي");

        jdbc.update(
                """
                insert into public.users (id, name, email, password, status, type)
                values ('00000000-0000-0000-0000-000000000901', 'Case User', 'Case.Unique@example.com', 'encoded-password', 'active', 'member')
                """);
        assertThatThrownBy(() -> jdbc.update(
                """
                insert into public.users (id, name, email, password, status, type)
                values ('00000000-0000-0000-0000-000000000902', 'Case User 2', ' case.unique@EXAMPLE.com ', 'encoded-password', 'active', 'member')
                """))
                .hasMessageContaining("uq_users_email_lower");
    }
}
