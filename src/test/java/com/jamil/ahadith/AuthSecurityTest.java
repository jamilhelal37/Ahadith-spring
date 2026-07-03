package com.jamil.ahadith;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void registerAndLoginShouldBeAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test User\",\"email\":\"test@example.com\",\"password\":\"123456\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\",\"password\":\"123456\"}"))
                .andExpect(status().isOk());
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
}
