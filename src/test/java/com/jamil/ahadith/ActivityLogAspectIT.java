package com.jamil.ahadith;

import com.jamil.ahadith.dtos.requests.TopicRequestDto;
import com.jamil.ahadith.entities.User;
import com.jamil.ahadith.entities.UserStatus;
import com.jamil.ahadith.entities.UserType;
import com.jamil.ahadith.repositories.ActivityLogRepository;
import com.jamil.ahadith.repositories.UserRepository;
import com.jamil.ahadith.services.TopicService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ActivityLogAspectIT extends PostgresIntegrationTestBase {

    @Autowired
    private TopicService topicService;

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createTopicShouldPersistActivityLogForAdminWithPostgresJsonData() {
        User admin = new User();
        admin.setName("Admin User");
        admin.setEmail("admin@example.com");
        admin.setPassword("encoded-password");
        admin.setStatus(UserStatus.active);
        admin.setType(UserType.admin);
        userRepository.saveAndFlush(admin);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "admin@example.com",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                )
        );

        TopicRequestDto request = new TopicRequestDto();
        request.setName("Logging Topic");

        topicService.createTopic(request);

        var logs = activityLogRepository.findAll();
        assertThat(logs).hasSize(1);
        var log = logs.get(0);
        assertThat(log.getActorEmail()).isEqualTo("admin@example.com");
        assertThat(log.getMessage()).contains("created");
        assertThat(log.getTableName()).isEqualTo("topics");
        assertThat(log.getNewData())
                .containsEntry("operation", "create")
                .containsEntry("service", "TopicService")
                .containsEntry("table", "topics");

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
                .isEqualTo("create");
    }
}
