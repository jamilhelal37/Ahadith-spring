package com.jamil.ahadith.features.auth;

import com.jamil.ahadith.features.user.entity.Gender;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jamil.ahadith.features.auth.dto.response.AuthResponseDto;
import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.user.entity.UserStatus;
import com.jamil.ahadith.features.user.entity.UserType;
import com.jamil.ahadith.features.user.repository.UserRepository;
import com.jamil.ahadith.core.security.jwt.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void registerAndLoginShouldBeAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test User\",\"email\":\"test@example.com\",\"password\":\"12345678\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.user.type").value("member"))
                .andExpect(jsonPath("$.user.status").value("pending_confirmation"))
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andExpect(jsonPath("$.user.password").doesNotExist())
                .andExpect(jsonPath("$.user.avatarPublicId").doesNotExist());

        User loginUser = new User();
        loginUser.setName("Login User");
        loginUser.setEmail("login@example.com");
        loginUser.setPassword(passwordEncoder.encode("12345678"));
        loginUser.setType(UserType.member);
        loginUser.setStatus(UserStatus.active);
        userRepository.save(loginUser);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"login@example.com\",\"password\":\"12345678\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value("login@example.com"))
                .andExpect(jsonPath("$.user.name").value("Login User"))
                .andExpect(jsonPath("$.user.password").doesNotExist())
                .andExpect(jsonPath("$.user.avatarPublicId").doesNotExist());
    }

    @Test
    void registerShouldAcceptBirthDateInDdMmYyyyFormat() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Jamil helal\",\"email\":\"jam@gmail.com\",\"password\":\"12345678\",\"gender\":\"male\",\"birthDate\":\"01/01/1994\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void registerShouldIgnoreAuthorizationHeader() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .header("Authorization", "Bearer invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Auth Header User\",\"email\":\"auth-header@example.com\",\"password\":\"12345678\",\"gender\":\"male\",\"birthDate\":\"01-01-1994\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void registerShouldAcceptUppercaseGenderAndIsoBirthDate() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Iso User\",\"email\":\"iso@example.com\",\"password\":\"12345678\",\"gender\":\"MALE\",\"birthDate\":\"1994-01-01\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void registerShouldRejectDuplicateEmailAfterNormalization() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"First User\",\"email\":\"Jamil.Case@example.com\",\"password\":\"12345678\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value("jamil.case@example.com"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Second User\",\"email\":\"jamil.case@EXAMPLE.com\",\"password\":\"12345678\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email is already registered"));
    }

    @Test
    void refreshShouldUseBodyRefreshTokenWithoutAuthorizationHeader() throws Exception {
        User user = new User();
        user.setName("Refresh User");
        user.setEmail("refresh@example.com");
        user.setPassword(passwordEncoder.encode("12345678"));
        user.setType(UserType.member);
        user.setStatus(UserStatus.active);
        userRepository.save(user);

        var loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"refresh@example.com\",\"password\":\"12345678\"}"))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponseDto authResponse = objectMapper.readValue(
                loginResult.getResponse().getContentAsString(),
                AuthResponseDto.class
        );

        mockMvc.perform(post("/auth/refresh")
                        .header("Authorization", "Bearer invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + authResponse.getRefreshToken() + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andExpect(jsonPath("$.user.email").value("refresh@example.com"))
                .andExpect(jsonPath("$.user.type").value("member"))
                .andExpect(jsonPath("$.user.password").doesNotExist())
                .andExpect(jsonPath("$.user.avatarPublicId").doesNotExist());
    }

    @Test
    void refreshShouldRejectInvalidRefreshTokenWithClearMessage() throws Exception {
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"invalid-token\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid or expired refresh token"));
    }

    @Test
    void adminEndpointsShouldRejectMember() throws Exception {
        String token = accessTokenFor("member-admin-check@example.com", UserType.member);

        mockMvc.perform(get("/admin/security-check")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminEndpointsShouldRejectScholar() throws Exception {
        String token = accessTokenFor("scholar-admin-check@example.com", UserType.scholar);

        mockMvc.perform(get("/admin/security-check")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminEndpointsShouldAllowAdminThroughSecurity() throws Exception {
        String token = accessTokenFor("admin-security-check@example.com", UserType.admin);

        mockMvc.perform(get("/admin/security-check")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void scholarEndpointsShouldRejectMemberAndAllowScholarOrAdminThroughSecurity() throws Exception {
        String memberToken = accessTokenFor("member-scholar-check@example.com", UserType.member);
        String scholarToken = accessTokenFor("scholar-scholar-check@example.com", UserType.scholar);
        String adminToken = accessTokenFor("admin-scholar-check@example.com", UserType.admin);

        mockMvc.perform(get("/scholar/security-check")
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/scholar/security-check")
                        .header("Authorization", "Bearer " + scholarToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/scholar/security-check")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    private String accessTokenFor(String email, UserType type) {
        User user = new User();
        user.setName("Security Test User");
        user.setEmail(email);
        user.setPassword("encoded-password");
        user.setType(type);
        user.setStatus(UserStatus.active);
        user = userRepository.save(user);
        return jwtService.generateAccessToken(user);
    }
}
