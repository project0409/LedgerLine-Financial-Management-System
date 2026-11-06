package com.ledgerline.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledgerline.dto.request.CreateAccountRequest;
import com.ledgerline.entity.Account;
import com.ledgerline.entity.AccountPermission;
import com.ledgerline.entity.Role;
import com.ledgerline.entity.User;
import com.ledgerline.repository.AccountPermissionRepository;
import com.ledgerline.repository.AccountRepository;
import com.ledgerline.repository.RoleRepository;
import com.ledgerline.repository.UserRepository;
import com.ledgerline.security.JwtTokenProvider;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountPermissionRepository accountPermissionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser1;
    private User testUser2;
    private String user1Token;
    private String user2Token;

    @BeforeEach
    void setUp() {
        Role holderRole = roleRepository.findByRoleName("ROLE_ACCOUNT_HOLDER").orElseThrow();

        testUser1 = userRepository.findByEmailIgnoreCase("acc.user1@example.com").orElseGet(() ->
                userRepository.save(User.builder()
                        .fullName("Account Holder One")
                        .email("acc.user1@example.com")
                        .passwordHash(passwordEncoder.encode("Secret123!"))
                        .enabled(true)
                        .roles(Set.of(holderRole))
                        .build()));

        testUser2 = userRepository.findByEmailIgnoreCase("acc.user2@example.com").orElseGet(() ->
                userRepository.save(User.builder()
                        .fullName("Account Holder Two")
                        .email("acc.user2@example.com")
                        .passwordHash(passwordEncoder.encode("Secret123!"))
                        .enabled(true)
                        .roles(Set.of(holderRole))
                        .build()));

        user1Token = "Bearer " + jwtTokenProvider.generateTokenFromUser(testUser1.getEmail(), testUser1.getId(), List.of("ROLE_ACCOUNT_HOLDER"));
        user2Token = "Bearer " + jwtTokenProvider.generateTokenFromUser(testUser2.getEmail(), testUser2.getId(), List.of("ROLE_ACCOUNT_HOLDER"));
    }

    @Test
    @DisplayName("Test 13: Invalid account creation rejection (validation checks)")
    void shouldRejectInvalidAccountCreation() throws Exception {
        // Missing account name and invalid account type
        CreateAccountRequest invalidRequest = CreateAccountRequest.builder()
                .accountName("")
                .accountType("INVALID_TYPE")
                .currency("USDD")
                .build();

        mockMvc.perform(post("/api/accounts")
                        .header("Authorization", user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.validationErrors.accountName").exists())
                .andExpect(jsonPath("$.validationErrors.accountType").exists());
    }

    @Test
    @DisplayName("Account creation initializes with zero balance and grants creator permissions")
    void shouldCreateAccountWithZeroBalanceAndPermissions() throws Exception {
        CreateAccountRequest request = CreateAccountRequest.builder()
                .accountName("Operational Savings")
                .accountType("SAVINGS")
                .currency("USD")
                .build();

        String responseBody = mockMvc.perform(post("/api/accounts")
                        .header("Authorization", user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.balance").value(0.00))
                .andExpect(jsonPath("$.data.accountNumber").isString())
                .andReturn().getResponse().getContentAsString();

        Long accountId = objectMapper.readTree(responseBody).path("data").path("id").asLong();

        // Verify account permission recorded
        AccountPermission permission = accountPermissionRepository.findByAccountIdAndUserId(accountId, testUser1.getId()).orElseThrow();
        assertThat(permission.getCanView()).isTrue();
        assertThat(permission.getCanDeposit()).isTrue();
        assertThat(permission.getCanWithdraw()).isTrue();
        assertThat(permission.getCanTransfer()).isTrue();
    }

    @Test
    @DisplayName("Account access control: Users cannot view accounts they lack permissions for")
    void shouldDenyAccessToUnauthorizedAccount() throws Exception {
        // Create account for user 1
        Account account = accountRepository.save(Account.builder()
                .accountNumber("LL-PRIV-" + System.currentTimeMillis())
                .accountName("Private Vault")
                .accountType("CHECKING")
                .balance(BigDecimal.ZERO.setScale(2))
                .build());

        accountPermissionRepository.save(AccountPermission.builder()
                .account(account)
                .user(testUser1)
                .canView(true)
                .build());

        // User 2 attempts to access User 1's account
        mockMvc.perform(get("/api/accounts/" + account.getId())
                        .header("Authorization", user2Token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));

        // User 1 can access it
        mockMvc.perform(get("/api/accounts/" + account.getId())
                        .header("Authorization", user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accountName").value("Private Vault"));
    }
}
