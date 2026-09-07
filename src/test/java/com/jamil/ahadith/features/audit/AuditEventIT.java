package com.jamil.ahadith.features.audit;

import com.jamil.ahadith.core.PostgresIntegrationTestBase;
import com.jamil.ahadith.features.audit.repository.ActivityLogRepository;
import com.jamil.ahadith.features.catalog.dto.request.TopicRequestDto;
import com.jamil.ahadith.features.catalog.dto.update.TopicUpdateDto;
import com.jamil.ahadith.features.catalog.service.TopicService;
import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.user.entity.UserStatus;
import com.jamil.ahadith.features.user.entity.UserType;
import com.jamil.ahadith.features.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuditEventIT
        extends PostgresIntegrationTestBase {

    @Autowired
    private TopicService topicService;

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private User admin;

    @BeforeEach
    void setUp() {
        activityLogRepository.deleteAll();

        User user = new User();

        user.setName(
                "Admin User"
        );

        user.setEmail(
                "admin@example.com"
        );

        user.setPassword(
                "encoded-password"
        );

        user.setStatus(
                UserStatus.active
        );

        user.setType(
                UserType.admin
        );

        admin =
                userRepository
                        .saveAndFlush(user);

        SecurityContextHolder
                .getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                admin.getEmail(),
                                null,
                                List.of(
                                        new SimpleGrantedAuthority(
                                                "ROLE_ADMIN"
                                        )
                                )
                        )
                );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createTopicShouldPersistActivityLogForAdminWithPostgresJsonData() {
        TopicRequestDto request =
                new TopicRequestDto();

        request.setName(
                "Logging Topic"
        );

        topicService.createTopic(request);

        var logs =
                activityLogRepository.findAll();

        assertThat(logs)
                .hasSize(1);

        var log =
                logs.get(0);

        assertThat(
                log.getActorEmail()
        ).isEqualTo(
                "admin@example.com"
        );

        assertThat(
                log.getMessage()
        ).isEqualTo(
                "تمت إضافة موضوع جديد إلى الموسوعة"
        );

        assertThat(
                log.getTableName()
        ).isEqualTo(
                "topics"
        );

        assertThat(
                log.getRecordId()
        ).isNotNull();

        assertThat(
                log.getNewData()
        )
                .containsEntry(
                        "operation",
                        "CREATE"
                )
                .containsEntry(
                        "table",
                        "topics"
                )
                .containsEntry(
                        "name",
                        "Logging Topic"
                );

        assertThat(
                jdbc.queryForObject(
                        """
                        select jsonb_typeof(old_data)
                        from public.activity_log
                        where id = ?
                        """,
                        String.class,
                        log.getId()
                )
        ).isEqualTo(
                "object"
        );

        assertThat(
                jdbc.queryForObject(
                        """
                        select jsonb_typeof(new_data)
                        from public.activity_log
                        where id = ?
                        """,
                        String.class,
                        log.getId()
                )
        ).isEqualTo(
                "object"
        );

        assertThat(
                jdbc.queryForObject(
                        """
                        select new_data ->> 'operation'
                        from public.activity_log
                        where id = ?
                        """,
                        String.class,
                        log.getId()
                )
        ).isEqualTo(
                "CREATE"
        );
    }

    @Test
    void updateAndDeleteShouldPersistOldAndNewData() {
        TopicRequestDto create =
                new TopicRequestDto();

        create.setName("Before");

        var topic =
                topicService.createTopic(create);

        activityLogRepository.deleteAll();

        TopicUpdateDto update =
                new TopicUpdateDto();

        update.setName("After");

        topicService.updateTopic(
                topic.getId(),
                update
        );

        var updateLog =
                activityLogRepository
                        .findAll()
                        .getFirst();

        assertThat(
                updateLog.getMessage()
        ).isEqualTo(
                "تم تعديل بيانات موضوع"
        );

        assertThat(
                updateLog.getOldData()
        ).containsEntry(
                "name",
                "Before"
        );

        assertThat(
                updateLog.getNewData()
        ).containsEntry(
                "name",
                "After"
        );

        assertThat(
                updateLog.getOldData()
        ).containsEntry(
                "operation",
                "UPDATE"
        );

        assertThat(
                updateLog.getNewData()
        ).containsEntry(
                "operation",
                "UPDATE"
        );

        activityLogRepository.deleteAll();

        topicService.deleteTopic(
                topic.getId()
        );

        var deleteLog =
                activityLogRepository
                        .findAll()
                        .getFirst();

        assertThat(
                deleteLog.getMessage()
        ).isEqualTo(
                "تم حذف موضوع من الموسوعة"
        );

        assertThat(
                deleteLog.getRecordId()
        ).isEqualTo(
                topic.getId()
        );

        assertThat(
                deleteLog.getOldData()
        ).containsEntry(
                "name",
                "After"
        );

        assertThat(
                deleteLog.getOldData()
        ).containsEntry(
                "operation",
                "DELETE"
        );
    }

    @Test
    void rollbackShouldNotPersistAuditLog() {
        try {
            transactionTemplate
                    .executeWithoutResult(
                            status -> {
                                TopicRequestDto request =
                                        new TopicRequestDto();

                                request.setName(
                                        "Rollback Topic"
                                );

                                topicService
                                        .createTopic(
                                                request
                                        );

                                throw new IllegalStateException(
                                        "rollback"
                                );
                            }
                    );
        } catch (IllegalStateException ignored) {
        }

        assertThat(
                activityLogRepository.findAll()
        ).isEmpty();
    }

    @Test
    void activityLogFailureShouldRollbackBusinessChange() {
        installFailingActivityLogTrigger();

        try {
            TopicRequestDto request =
                    new TopicRequestDto();

            request.setName(
                    "Atomicity Topic"
            );

            assertThatThrownBy(
                    () -> topicService
                            .createTopic(
                                    request
                            )
            ).isInstanceOf(
                    RuntimeException.class
            );

            assertThat(
                    countRows(
                            "public.topics",
                            "name = 'Atomicity Topic'"
                    )
            ).isZero();

            assertThat(
                    countRows(
                            "public.activity_log",
                            "true"
                    )
            ).isZero();

        } finally {
            dropFailingActivityLogTrigger();
        }
    }

    @Test
    void auditSnapshotsShouldNotExposeSecrets() {
        admin.setGoogleSubject(
                "google-subject"
        );

        Map<String, Object> snapshot =
                com.jamil.ahadith.features.audit.service.AuditData
                        .snapshot(admin);

        assertThat(snapshot)
                .doesNotContainKeys(
                        "password",
                        "token",
                        "tokenHash",
                        "googleSubject"
                );
    }

    private void installFailingActivityLogTrigger() {
        jdbc.execute(
                """
                create or replace function public.fail_activity_log_insert()
                returns trigger
                language plpgsql
                as $$
                begin
                    raise exception 'activity log failed';
                end;
                $$;
                """
        );

        jdbc.execute(
                """
                create trigger fail_activity_log_insert
                before insert on public.activity_log
                for each row
                execute function public.fail_activity_log_insert()
                """
        );
    }

    private void dropFailingActivityLogTrigger() {
        jdbc.execute(
                """
                drop trigger if exists
                fail_activity_log_insert
                on public.activity_log
                """
        );

        jdbc.execute(
                """
                drop function if exists
                public.fail_activity_log_insert()
                """
        );
    }

    private long countRows(
            String tableName,
            String condition
    ) {
        return jdbc.queryForObject(
                "select count(*) from "
                        + tableName
                        + " where "
                        + condition,
                Long.class
        );
    }
}