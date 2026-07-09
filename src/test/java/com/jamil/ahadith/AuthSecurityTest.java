package com.jamil.ahadith;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jamil.ahadith.dtos.responses.AuthResponseDto;
import com.jamil.ahadith.entities.User;
import com.jamil.ahadith.entities.UserType;
import com.jamil.ahadith.repositories.UserRepository;
import com.jamil.ahadith.services.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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

    @Test
    void registerAndLoginShouldBeAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test User\",\"email\":\"test@example.com\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.user.type").value("member"))
                .andExpect(jsonPath("$.user.password").doesNotExist())
                .andExpect(jsonPath("$.user.avatarPublicId").doesNotExist());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.user.name").value("Test User"))
                .andExpect(jsonPath("$.user.password").doesNotExist())
                .andExpect(jsonPath("$.user.avatarPublicId").doesNotExist());
    }

    @Test
    void registerShouldAcceptBirthDateInDdMmYyyyFormat() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Jamil helal\",\"email\":\"jam@gmail.com\",\"password\":\"123456\",\"gender\":\"male\",\"birthDate\":\"01/01/1994\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void registerShouldIgnoreAuthorizationHeader() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .header("Authorization", "Bearer invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Auth Header User\",\"email\":\"auth-header@example.com\",\"password\":\"123456\",\"gender\":\"male\",\"birthDate\":\"01-01-1994\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void registerShouldAcceptUppercaseGenderAndIsoBirthDate() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Iso User\",\"email\":\"iso@example.com\",\"password\":\"123456\",\"gender\":\"MALE\",\"birthDate\":\"1994-01-01\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void refreshShouldUseBodyRefreshTokenWithoutAuthorizationHeader() throws Exception {
        var loginResult = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Refresh User\",\"email\":\"refresh@example.com\",\"password\":\"123456\"}"))
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
                .andExpect(jsonPath("$.expiresIn").value(86400000))
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
    void adminEndpointsShouldAllowAdminThroughSecurity() throws Exception {
        String token = accessTokenFor("admin-security-check@example.com", UserType.admin);

        mockMvc.perform(get("/admin/security-check")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    private String accessTokenFor(String email, UserType type) {
        User user = new User();
        user.setName("Security Test User");
        user.setEmail(email);
        user.setPassword("encoded-password");
        user.setType(type);
        user = userRepository.save(user);
        return jwtService.generateAccessToken(user);
    }
}
