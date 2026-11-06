package com.ledgerline.security;

import com.ledgerline.entity.Role;
import com.ledgerline.entity.User;
import com.ledgerline.repository.RoleRepository;
import com.ledgerline.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String holderToken;
    private String auditorToken;

    @BeforeEach
    void setUp() {
        Role adminRole = roleRepository.findByRoleName("ROLE_ADMIN").orElseThrow();
        Role holderRole = roleRepository.findByRoleName("ROLE_ACCOUNT_HOLDER").orElseThrow();
        Role auditorRole = roleRepository.findByRoleName("ROLE_AUDITOR").orElseThrow();

        User admin = userRepository.findByEmailIgnoreCase("authz.admin@example.com").orElseGet(() ->
                userRepository.save(User.builder()
                        .fullName("Authz Admin")
                        .email("authz.admin@example.com")
                        .passwordHash(passwordEncoder.encode("Pass123!"))
                        .enabled(true)
                        .roles(Set.of(adminRole))
                        .build()));

        User holder = userRepository.findByEmailIgnoreCase("authz.holder@example.com").orElseGet(() ->
                userRepository.save(User.builder()
                        .fullName("Authz Holder")
                        .email("authz.holder@example.com")
                        .passwordHash(passwordEncoder.encode("Pass123!"))
                        .enabled(true)
                        .roles(Set.of(holderRole))
                        .build()));

        User auditor = userRepository.findByEmailIgnoreCase("authz.auditor@example.com").orElseGet(() ->
                userRepository.save(User.builder()
                        .fullName("Authz Auditor")
                        .email("authz.auditor@example.com")
                        .passwordHash(passwordEncoder.encode("Pass123!"))
                        .enabled(true)
                        .roles(Set.of(auditorRole))
                        .build()));

        adminToken = "Bearer " + jwtTokenProvider.generateTokenFromUser(admin.getEmail(), admin.getId(), List.of("ROLE_ADMIN"));
        holderToken = "Bearer " + jwtTokenProvider.generateTokenFromUser(holder.getEmail(), holder.getId(), List.of("ROLE_ACCOUNT_HOLDER"));
        auditorToken = "Bearer " + jwtTokenProvider.generateTokenFromUser(auditor.getEmail(), auditor.getId(), List.of("ROLE_AUDITOR"));
    }

    @Test
    @DisplayName("Test 8: Unauthorized endpoint rejection (401 without JWT)")
    void shouldRejectAccessWithoutAuthenticationToken() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Test 9: Administrator endpoint access allowed for ROLE_ADMIN")
    void shouldAllowAdminAccessToAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        mockMvc.perform(get("/api/admin/roles")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Test 10: Account holder access restrictions (403 on admin routes)")
    void shouldForbidAccountHolderFromAccessingAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", holderToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(get("/api/admin/roles")
                        .header("Authorization", holderToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Test 11: Auditor read-only permissions and admin restriction")
    void shouldAllowAuditorToViewAccountsButForbidAdminEndpoints() throws Exception {
        // Auditor CAN view accounts (read-only financial oversight)
        mockMvc.perform(get("/api/accounts")
                        .header("Authorization", auditorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Auditor CANNOT access administrative governance endpoints
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", auditorToken))
                .andExpect(status().isForbidden());
    }
}
