package com.footballacademy.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.footballacademy.model.User;
import com.footballacademy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @BeforeEach void setUp() {
        // Clean up database before each test
        userRepository.deleteAll();
    }
    @Test void testSignup_Success() throws Exception {
        // Arrange
        String signupJson = "" "             {                 " nom ": " Test User ",                 " email ": " test
        @example.com ",                 " mdp ": " Test
        @1234 ",                 " mainRole ": " PLAYER "             }             " "";
        // Act
        MvcResult result = mockMvc.perform(post("/api/auth/signup") .contentType(MediaType.APPLICATION_JSON) .content(signupJson)) .andExpect(status() .isCreated()) .andExpect(jsonPath("$.nom") .value("Test User")) .andExpect(jsonPath("$.email") .value("test@example.com")) .andReturn();
        // Assert
        String response = result.getResponse() .getContentAsString();
        assertNotNull(response);
        // Verify user was created in database
        User user = userRepository.findByEmail("test@example.com") .orElse(null);
        assertNotNull(user);
        assertEquals("Test User", user.getNom());
    }
    @Test void testSignup_InvalidEmail() throws Exception {
        // Arrange
        String signupJson = "" "             {                 " nom ": " Test User ",                 " email ": " invalid-email ",                 " mdp ": " Test
        @1234 ",                 " mainRole ": " PLAYER "             }             " "";
        // Act & Assert
        mockMvc.perform(post("/api/auth/signup") .contentType(MediaType.APPLICATION_JSON) .content(signupJson)) .andExpect(status() .isBadRequest());
    }
    @Test void testSignup_WeakPassword() throws Exception {
        // Arrange
        String signupJson = "" "             {                 " nom ": " Test User ",                 " email ": " test
        @example.com ",                 " mdp ": " weak ",                 " mainRole ": " PLAYER "             }             " "";
        // Act & Assert
        mockMvc.perform(post("/api/auth/signup") .contentType(MediaType.APPLICATION_JSON) .content(signupJson)) .andExpect(status() .isBadRequest());
    }
    @Test void testSignup_DuplicateEmail() throws Exception {
        // Arrange - Create user first
        User existingUser = new User();
        existingUser.setNom("Existing User");
        existingUser.setEmail("test@example.com");
        existingUser.setMdp("Test@1234");
        existingUser.setMainRole(User.UserRole.PLAYER);
        userRepository.save(existingUser);
        String signupJson = "" "             {                 " nom ": " Test User ",                 " email ": " test
        @example.com ",                 " mdp ": " Test
        @1234 ",                 " mainRole ": " PLAYER "             }             " "";
        // Act & Assert
        mockMvc.perform(post("/api/auth/signup") .contentType(MediaType.APPLICATION_JSON) .content(signupJson)) .andExpect(status() .isBadRequest());
    }
    @Test void testLogin_Success() throws Exception {
        // Arrange - Create user first
        User user = new User();
        user.setNom("Test User");
        user.setEmail("test@example.com");
        user.setMdp("Test@1234");
        // In real app, this would be encoded
        user.setMainRole(User.UserRole.PLAYER);
        userRepository.save(user);
        String loginJson = "" "             {                 " email ": " test
        @example.com ",                 " password ": " Test
        @1234 "             }             " "";
        // Act & Assert
        mockMvc.perform(post("/api/auth/login") .contentType(MediaType.APPLICATION_JSON) .content(loginJson)) .andExpect(status() .isOk()) .andExpect(jsonPath("$.user.email") .value("test@example.com"));
    }
    @Test void testLogin_InvalidCredentials() throws Exception {
        // Arrange
        String loginJson = "" "             {                 " email ": " nonexistent
        @example.com ",                 " password ": " wrongpassword "             }             " "";
        // Act & Assert
        mockMvc.perform(post("/api/auth/login") .contentType(MediaType.APPLICATION_JSON) .content(loginJson)) .andExpect(status() .isUnauthorized());
    }
    @Test void testValidateToken_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/auth/validate") .header("Authorization", "Bearer valid-token")) .andExpect(status() .isOk());
    }
    @Test void testValidateToken_MissingToken() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/auth/validate")) .andExpect(status() .isOk()) .andExpect(jsonPath("$.valid") .value(false));
    }
}
