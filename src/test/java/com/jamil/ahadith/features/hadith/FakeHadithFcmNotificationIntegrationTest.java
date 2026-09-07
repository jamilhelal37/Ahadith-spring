package com.jamil.ahadith.features.hadith;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jamil.ahadith.core.security.jwt.JwtService;
import com.jamil.ahadith.features.notification.fcm.FcmNotificationPayload;
import com.jamil.ahadith.features.notification.fcm.FcmSendResult;
import com.jamil.ahadith.features.notification.fcm.FcmSender;
import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.user.entity.UserStatus;
import com.jamil.ahadith.features.user.entity.UserType;
import com.jamil.ahadith.features.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FakeHadithFcmNotificationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbc;

    @MockitoBean
    private FcmSender fcmSender;

    @BeforeEach
    void setUp() {
        jdbc.execute(
                "delete from \"activity_log\""
        );
        jdbc.execute(
                "delete from \"user_fcm_tokens\""
        );
        jdbc.execute(
                "delete from \"notifications\""
        );
        jdbc.execute(
                "delete from \"fake_ahadith\""
        );
        jdbc.execute(
                "delete from \"ruling\""
        );
        jdbc.execute(
                "delete from \"users\""
        );

        reset(fcmSender);

        when(
                fcmSender.send(
                        anyList(),
                        any()
                )
        ).thenReturn(
                FcmSendResult.empty()
        );
    }

    @Test
    void createFakeHadithShouldSendFcmAfterCommitWithoutPersistingNotification()
            throws Exception {

        User admin = user(
                "fake-admin@example.com",
                UserType.admin,
                UserStatus.active
        );

        User activeUser = user(
                "active-device@example.com",
                UserType.member,
                UserStatus.active
        );

        User disabledUser = user(
                "disabled-device@example.com",
                UserType.member,
                UserStatus.disabled
        );

        insertFcmToken(
                activeUser.getId(),
                "active-token"
        );

        insertFcmToken(
                disabledUser.getId(),
                "disabled-token"
        );

        UUID rulingId = ruling();

        String fakeHadithText =
                "هذا نص حديث منتشر لا يصح";

        JsonNode created = createFakeHadith(
                admin,
                rulingId,
                fakeHadithText
        );

        UUID fakeHadithId =
                UUID.fromString(
                        created.get("id").asText()
                );

        ArgumentCaptor<List<String>> tokensCaptor =
                ArgumentCaptor.forClass(List.class);

        ArgumentCaptor<FcmNotificationPayload> payloadCaptor =
                ArgumentCaptor.forClass(
                        FcmNotificationPayload.class
                );

        verify(fcmSender).send(
                tokensCaptor.capture(),
                payloadCaptor.capture()
        );

        assertThat(tokensCaptor.getValue())
                .containsExactly(
                        "active-token"
                );

        FcmNotificationPayload payload =
                payloadCaptor.getValue();

        assertThat(payload.title())
                .isEqualTo(
                        "انتبه حديث منتشر لا يصح"
                );

        assertThat(payload.body())
                .isEqualTo(fakeHadithText);

        assertThat(payload.data())
                .containsExactlyInAnyOrderEntriesOf(
                        Map.of(
                                "type",
                                "fake_hadith",
                                "fakeHadithId",
                                fakeHadithId.toString()
                        )
                );

        assertThat(payload.data())
                .doesNotContainKey("text");

        assertThat(
                countRows(
                        "\"notifications\"",
                        "true"
                )
        ).isZero();
    }

    @Test
    void updateAndDeleteFakeHadithShouldNotSendFcm()
            throws Exception {

        User admin = user(
                "fake-update-admin@example.com",
                UserType.admin,
                UserStatus.active
        );

        User activeUser = user(
                "fake-update-device@example.com",
                UserType.member,
                UserStatus.active
        );

        insertFcmToken(
                activeUser.getId(),
                "active-token"
        );

        UUID rulingId = ruling();

        JsonNode created = createFakeHadith(
                admin,
                rulingId,
                "Fake hadith before update"
        );

        clearInvocations(fcmSender);

        mockMvc.perform(
                        put(
                                "/api/v1/admin/fake-ahadith/"
                                        + created
                                        .get("id")
                                        .asText()
                        )
                                .header(
                                        "Authorization",
                                        bearer(admin)
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        "{\"text\":\"Fake hadith after update\"}"
                                )
                )
                .andExpect(
                        status().isOk()
                );

        mockMvc.perform(
                        delete(
                                "/api/v1/admin/fake-ahadith/"
                                        + created
                                        .get("id")
                                        .asText()
                        )
                                .header(
                                        "Authorization",
                                        bearer(admin)
                                )
                )
                .andExpect(
                        status().isNoContent()
                );

        verify(
                fcmSender,
                never()
        ).send(
                anyList(),
                any()
        );
    }

    @Test
    void createFakeHadithShouldNotCallFirebaseWhenNoTokensExist()
            throws Exception {

        User admin = user(
                "fake-no-token-admin@example.com",
                UserType.admin,
                UserStatus.active
        );

        UUID rulingId = ruling();

        createFakeHadith(
                admin,
                rulingId,
                "Fake hadith without registered devices"
        );

        verify(
                fcmSender,
                never()
        ).send(
                anyList(),
                any()
        );
    }

    @Test
    void createFakeHadithShouldDeleteInvalidTokensReturnedByFcm()
            throws Exception {

        User admin = user(
                "fake-invalid-admin@example.com",
                UserType.admin,
                UserStatus.active
        );

        User activeUser = user(
                "fake-invalid-device@example.com",
                UserType.member,
                UserStatus.active
        );

        insertFcmToken(
                activeUser.getId(),
                "valid-token"
        );

        insertFcmToken(
                activeUser.getId(),
                "invalid-token"
        );

        when(
                fcmSender.send(
                        anyList(),
                        any()
                )
        ).thenReturn(
                new FcmSendResult(
                        1,
                        1,
                        Set.of("invalid-token")
                )
        );

        createFakeHadith(
                admin,
                ruling(),
                "Fake hadith invalid token cleanup"
        );

        assertThat(
                tokenExists("valid-token")
        ).isTrue();

        assertThat(
                tokenExists("invalid-token")
        ).isFalse();
    }

    @Test
    void firebaseFailureShouldNotMakeCreateEndpointFailOrRollbackFakeHadith()
            throws Exception {

        User admin = user(
                "fake-firebase-fail-admin@example.com",
                UserType.admin,
                UserStatus.active
        );

        User activeUser = user(
                "fake-firebase-fail-device@example.com",
                UserType.member,
                UserStatus.active
        );

        insertFcmToken(
                activeUser.getId(),
                "active-token"
        );

        when(
                fcmSender.send(
                        anyList(),
                        any()
                )
        ).thenThrow(
                new RuntimeException(
                        "firebase unavailable"
                )
        );

        JsonNode created = createFakeHadith(
                admin,
                ruling(),
                "Fake hadith survives firebase failure"
        );

        assertThat(
                countRows(
                        "\"fake_ahadith\"",
                        "id = '"
                                + created
                                .get("id")
                                .asText()
                                + "'"
                )
        ).isOne();

        assertThat(
                countRows(
                        "\"notifications\"",
                        "true"
                )
        ).isZero();
    }

    private JsonNode createFakeHadith(
            User admin,
            UUID rulingId,
            String text
    ) throws Exception {

        String response =
                mockMvc.perform(
                                post(
                                        "/api/v1/admin/fake-ahadith"
                                )
                                        .header(
                                                "Authorization",
                                                bearer(admin)
                                        )
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content(
                                                """
                                                {"text":"%s","ruling":{"id":"%s"}}
                                                """.formatted(
                                                        text,
                                                        rulingId
                                                )
                                        )
                        )
                        .andExpect(
                                status().isCreated()
                        )
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        return objectMapper.readTree(response);
    }

    private User user(
            String email,
            UserType type,
            UserStatus status
    ) {
        User user = new User();

        user.setName(email);
        user.setEmail(email);
        user.setPassword(
                passwordEncoder.encode(
                        "12345678"
                )
        );
        user.setStatus(status);
        user.setType(type);

        return userRepository.saveAndFlush(user);
    }

    private UUID ruling() {
        UUID id = UUID.randomUUID();

        jdbc.update(
                "insert into \"ruling\" (id, name) values (?, ?)",
                id,
                "Weak"
        );

        return id;
    }

    private void insertFcmToken(
            UUID userId,
            String token
    ) {
        jdbc.update(
                """
                insert into "user_fcm_tokens"
                    (id, user_id, fcm_token, last_seen)
                values (?, ?, ?, current_timestamp)
                """,
                UUID.randomUUID(),
                userId,
                token
        );
    }

    private boolean tokenExists(String token) {
        return countRows(
                "\"user_fcm_tokens\"",
                "fcm_token = '" + token + "'"
        ) > 0;
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

    private String bearer(User user) {
        return "Bearer "
                + jwtService.generateAccessToken(user);
    }
}