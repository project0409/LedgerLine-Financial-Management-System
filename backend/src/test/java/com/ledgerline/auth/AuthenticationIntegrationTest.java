package com.ledgerline.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledgerline.dto.request.LoginRequest;
import com.ledgerline.dto.request.RegisterRequest;
import com.ledgerline.entity.User;
import com.ledgerline.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.findByEmailIgnoreCase("reg.test@example.com").ifPresent(userRepository::delete);
    }

    @Test
    @DisplayName("Test 1 & 12: Successful registration assigns default ROLE_ACCOUNT_HOLDER without role tampering")
    void shouldRegisterNewUserSuccessfully() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Sarah Jenkins")
                .email("reg.test@example.com")
                .phoneNumber("+1-555-0199")
                .password("SecurePass123!")
                .confirmPassword("SecurePass123!")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("reg.test@example.com"))
                .andExpect(jsonPath("$.data.roles[0]").value("ROLE_ACCOUNT_HOLDER"));

        User savedUser = userRepository.findByEmailIgnoreCase("reg.test@example.com").orElseThrow();
        assertThat(savedUser.getFullName()).isEqualTo("Sarah Jenkins");
        assertThat(savedUser.getRoles()).extracting("roleName").containsExactly("ROLE_ACCOUNT_HOLDER");
    }

    @Test
    @DisplayName("Test 2: Duplicate email registration rejection")
    void shouldRejectDuplicateEmailRegistration() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("First User")
                .email("reg.test@example.com")
                .password("SecurePass123!")
                .confirmPassword("SecurePass123!")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Attempt second registration with same email
        RegisterRequest duplicateRequest = RegisterRequest.builder()
                .fullName("Second User")
                .email("reg.test@example.com")
                .password("AnotherPassword123!")
                .confirmPassword("AnotherPassword123!")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    @DisplayName("Test 3: Password hashing verification (passwords never stored in plain text)")
    void shouldHashPasswordWithBCrypt() throws Exception {
        String plainPassword = "SuperSecretPassword99!";
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Crypto User")
                .email("reg.test@example.com")
                .password(plainPassword)
                .confirmPassword(plainPassword)
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        User savedUser = userRepository.findByEmailIgnoreCase("reg.test@example.com").orElseThrow();
        assertThat(savedUser.getPasswordHash()).isNotEqualTo(plainPassword);
        assertThat(savedUser.getPasswordHash()).startsWith("$2a$");
        assertThat(passwordEncoder.matches(plainPassword, savedUser.getPasswordHash())).isTrue();
    }

    @Test
    @DisplayName("Test 4: Successful login with valid credentials")
    void shouldLoginSuccessfullyWithValidCredentials() throws Exception {
        RegisterRequest register = RegisterRequest.builder()
                .fullName("Login Test User")
                .email("reg.test@example.com")
                .password("ValidPass123!")
                .confirmPassword("ValidPass123!")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());

        LoginRequest login = LoginRequest.builder()
                .email("reg.test@example.com")
                .password("ValidPass123!")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isString())
                .andExpect(jsonPath("$.data.user.email").value("reg.test@example.com"));
    }

    @Test
    @DisplayName("Test 5: Invalid password rejection")
    void shouldRejectLoginWithInvalidPassword() throws Exception {
        RegisterRequest register = RegisterRequest.builder()
                .fullName("Login Test User")
                .email("reg.test@example.com")
                .password("ValidPass123!")
                .confirmPassword("ValidPass123!")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());

        LoginRequest invalidLogin = LoginRequest.builder()
                .email("reg.test@example.com")
                .password("WrongPassword123!")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidLogin)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }
}
