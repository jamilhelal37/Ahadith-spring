package com.jamil.ahadith.features.hadith;

import com.jamil.ahadith.core.PostgresIntegrationTestBase;
import com.jamil.ahadith.core.security.jwt.JwtService;
import com.jamil.ahadith.features.notification.fcm.FcmSendResult;
import com.jamil.ahadith.features.notification.fcm.FcmSender;
import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.user.entity.UserStatus;
import com.jamil.ahadith.features.user.entity.UserType;
import com.jamil.ahadith.features.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class FakeHadithFcmNotificationIT extends PostgresIntegrationTestBase {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtService jwtService;
    @MockitoBean
    private FcmSender fcmSender;

    @Test
    void rollbackDuringCreateShouldNotSendFakeHadithFcmNotification() throws Exception {
        when(fcmSender.send(anyList(), any())).thenReturn(FcmSendResult.empty());
        User admin = user("rollback-fcm-admin@example.com", UserType.admin);
        User member = user("rollback-fcm-member@example.com", UserType.member);
        UUID rulingId = ruling();
        insertFcmToken(member.getId(), "member-token");
        installFailingActivityLogTrigger();
        try {
            mockMvc.perform(post("/api/v1/admin/fake-ahadith")
                            .header("Authorization", bearer(admin))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"text":"Fake hadith rollback should not notify","ruling":{"id":"%s"}}
                                    """.formatted(rulingId)))
                    .andExpect(status().isInternalServerError());

            assertThat(jdbc.queryForObject(
                    "select count(*) from public.fake_ahadith where text = ?",
                    Long.class,
                    "Fake hadith rollback should not notify"
            )).isZero();
            verify(fcmSender, never()).send(anyList(), any());
        } finally {
            dropFailingActivityLogTrigger();
        }
    }

    private User user(String email, UserType type) {
        User user = new User();
        user.setName(email);
        user.setEmail(email);
        user.setPassword("encoded-password");
        user.setStatus(UserStatus.active);
        user.setType(type);
        return userRepository.saveAndFlush(user);
    }

    private UUID ruling() {
        UUID id = UUID.randomUUID();
        jdbc.update("insert into public.ruling (id, name) values (?, ?)", id, "Weak");
        return id;
    }

    private void insertFcmToken(UUID userId, String token) {
        jdbc.update("""
                insert into public.user_fcm_tokens (id, user_id, fcm_token, last_seen)
                values (?, ?, ?, current_timestamp)
                """, UUID.randomUUID(), userId, token);
    }

    private void installFailingActivityLogTrigger() {
        jdbc.execute("""
                create or replace function public.fail_activity_log_insert()
                returns trigger
                language plpgsql
                as $$
                begin
                    raise exception 'activity log failed';
                end;
                $$;
                """);
        jdbc.execute("""
                create trigger fail_activity_log_insert
                before insert on public.activity_log
                for each row execute function public.fail_activity_log_insert()
                """);
    }

    private void dropFailingActivityLogTrigger() {
        jdbc.execute("drop trigger if exists fail_activity_log_insert on public.activity_log");
        jdbc.execute("drop function if exists public.fail_activity_log_insert()");
    }

    private String bearer(User user) {
        return "Bearer " + jwtService.generateAccessToken(user);
    }
}
