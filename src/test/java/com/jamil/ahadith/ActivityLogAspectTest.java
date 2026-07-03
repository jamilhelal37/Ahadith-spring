package com.jamil.ahadith;

import com.jamil.ahadith.dtos.requests.TopicRequestDto;
import com.jamil.ahadith.repositories.ActivityLogRepository;
import com.jamil.ahadith.repositories.UserRepository;
import com.jamil.ahadith.services.TopicService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ActivityLogAspectTest {

    @Autowired
    private TopicService topicService;

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        activityLogRepository.deleteAll();
        userRepository.deleteAll();

        jdbcTemplate.update(
                "insert into users (id, name, email, password, avatar_url, status, gender, type, birth_date, created_at, updated_at) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                UUID.randomUUID(),
                "Admin User",
                "admin@example.com",
                "encoded-password",
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createTopicShouldPersistActivityLogForAdmin() {
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
        assertThat(logs.get(0).getActorEmail()).isEqualTo("admin@example.com");
        assertThat(logs.get(0).getMessage()).contains("created");
        assertThat(logs.get(0).getTableName()).isEqualTo("topics");
    }
}
