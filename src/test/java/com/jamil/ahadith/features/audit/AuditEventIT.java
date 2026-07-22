package com.jamil.ahadith.features.audit;

import com.jamil.ahadith.core.PostgresIntegrationTestBase;

import com.jamil.ahadith.features.catalog.entity.Topic;

import com.jamil.ahadith.features.catalog.dto.request.TopicRequestDto;
import com.jamil.ahadith.features.catalog.dto.update.TopicUpdateDto;
import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.user.entity.UserStatus;
import com.jamil.ahadith.features.user.entity.UserType;
import com.jamil.ahadith.features.audit.repository.ActivityLogRepository;
import com.jamil.ahadith.features.user.repository.UserRepository;
import com.jamil.ahadith.features.catalog.service.TopicService;
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

class AuditEventIT extends PostgresIntegrationTestBase {

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
        user.setName("Admin User");
        user.setEmail("admin@example.com");
        user.setPassword("encoded-password");
        user.setStatus(UserStatus.active);
        user.setType(UserType.admin);
        admin = userRepository.saveAndFlush(user);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        admin.getEmail(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                )
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createTopicShouldPersistActivityLogForAdminWithPostgresJsonData() {
        TopicRequestDto request = new TopicRequestDto();
        request.setName("Logging Topic");

        topicService.createTopic(request);

        var logs = activityLogRepository.findAll();
        assertThat(logs).hasSize(1);
        var log = logs.get(0);
        assertThat(log.getActorEmail()).isEqualTo("admin@example.com");
        assertThat(log.getMessage()).contains("created");
        assertThat(log.getTableName()).isEqualTo("topics");
        assertThat(log.getRecordId()).isNotNull();
        assertThat(log.getNewData())
                .containsEntry("operation", "CREATE")
                .containsEntry("table", "topics")
                .containsEntry("name", "Logging Topic");

        assertThat(jdbc.queryForObject(
                "select jsonb_typeof(old_data) from public.activity_log where id = ?",
                String.class,
                log.getId()))
                .isEqualTo("object");
        assertThat(jdbc.queryForObject(
                "select jsonb_typeof(new_data) from public.activity_log where id = ?",
                String.class,
                log.getId()))
                .isEqualTo("object");
        assertThat(jdbc.queryForObject(
                "select new_data ->> 'operation' from public.activity_log where id = ?",
                String.class,
                log.getId()))
                .isEqualTo("CREATE");
    }

    @Test
    void updateAndDeleteShouldPersistOldAndNewData() {
        TopicRequestDto create = new TopicRequestDto();
        create.setName("Before");
        var topic = topicService.createTopic(create);
        activityLogRepository.deleteAll();

        TopicUpdateDto update = new TopicUpdateDto();
        update.setName("After");
        topicService.updateTopic(topic.getId(), update);

        var updateLog = activityLogRepository.findAll().getFirst();
        assertThat(updateLog.getMessage()).contains("updated");
        assertThat(updateLog.getOldData()).containsEntry("name", "Before");
        assertThat(updateLog.getNewData()).containsEntry("name", "After");

        activityLogRepository.deleteAll();
        topicService.deleteTopic(topic.getId());

        var deleteLog = activityLogRepository.findAll().getFirst();
        assertThat(deleteLog.getMessage()).contains("deleted");
        assertThat(deleteLog.getRecordId()).isEqualTo(topic.getId());
        assertThat(deleteLog.getOldData()).containsEntry("name", "After");
    }

    @Test
    void rollbackShouldNotPersistAuditLog() {
        try {
            transactionTemplate.executeWithoutResult(status -> {
                TopicRequestDto request = new TopicRequestDto();
                request.setName("Rollback Topic");
                topicService.createTopic(request);
                throw new IllegalStateException("rollback");
            });
        } catch (IllegalStateException ignored) {
        }

        assertThat(activityLogRepository.findAll()).isEmpty();
    }

    @Test
    void auditSnapshotsShouldNotExposeSecrets() {
        Map<String, Object> snapshot = com.jamil.ahadith.features.audit.service.AuditData.snapshot(admin);

        assertThat(snapshot).doesNotContainKeys("password", "token", "tokenHash");
    }
}
