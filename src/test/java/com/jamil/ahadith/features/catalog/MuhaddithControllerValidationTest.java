package com.jamil.ahadith.features.catalog;

import com.jamil.ahadith.core.security.jwt.JwtService;
import com.jamil.ahadith.features.catalog.repository.MuhaddithRepository;
import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.user.entity.UserStatus;
import com.jamil.ahadith.features.user.entity.UserType;
import com.jamil.ahadith.features.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MuhaddithControllerValidationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MuhaddithRepository muhaddithRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("delete from \"books\"");
        jdbcTemplate.execute("delete from \"muhaddiths\"");
        userRepository.deleteAll();
        adminToken = adminAccessToken();
    }

    @Test
    void createMuhaddithWithoutGenderShouldReturnBadRequestAndNotCreateRecord() throws Exception {
        assertValidationError(
                """
                {"name":"Validation Muhaddith","about":"Biography text"}
                """,
                "gender",
                "Muhaddith gender is required"
        );
    }

    @Test
    void createMuhaddithWithoutAboutShouldReturnBadRequestAndNotCreateRecord() throws Exception {
        assertValidationError(
                """
                {"name":"Validation Muhaddith","gender":"male"}
                """,
                "about",
                "Muhaddith about is required"
        );
    }

    @Test
    void createMuhaddithWithBlankAboutShouldReturnBadRequestAndNotCreateRecord() throws Exception {
        assertValidationError(
                """
                {"name":"Validation Muhaddith","gender":"male","about":""}
                """,
                "about",
                "Muhaddith about is required"
        );
    }

    @Test
    void createMuhaddithWithWhitespaceAboutShouldReturnBadRequestAndNotCreateRecord() throws Exception {
        assertValidationError(
                """
                {"name":"Validation Muhaddith","gender":"male","about":"   "}
                """,
                "about",
                "Muhaddith about is required"
        );
    }

    @Test
    void createMuhaddithWithoutNameShouldReturnBadRequestAndNotCreateRecord() throws Exception {
        assertValidationError(
                """
                {"gender":"male","about":"Biography text"}
                """,
                "name",
                "Muhaddith name is required"
        );
    }

    @Test
    void createMuhaddithWithRequiredFieldsShouldReturnCreated() throws Exception {
        mockMvc.perform(post("/admin/muhaddiths")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Created Muhaddith","gender":"male","about":"Biography text"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/api/v1/admin/muhaddiths/")))
                .andExpect(jsonPath("$.name").value("Created Muhaddith"))
                .andExpect(jsonPath("$.gender").value("male"))
                .andExpect(jsonPath("$.about").value("Biography text"));

        assertThat(muhaddithRepository.count()).isEqualTo(1);
    }

    private void assertValidationError(String requestBody, String field, String message) throws Exception {
        long countBefore = muhaddithRepository.count();

        mockMvc.perform(post("/admin/muhaddiths")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors." + field).value(message));

        assertThat(muhaddithRepository.count()).isEqualTo(countBefore);
    }

    private String adminAccessToken() {
        User user = new User();
        user.setName("Muhaddith Validation Admin");
        user.setEmail("muhaddith-validation-admin-" + UUID.randomUUID() + "@example.com");
        user.setPassword(passwordEncoder.encode("12345678"));
        user.setType(UserType.admin);
        user.setStatus(UserStatus.active);
        user = userRepository.saveAndFlush(user);
        return jwtService.generateAccessToken(user);
    }
}
