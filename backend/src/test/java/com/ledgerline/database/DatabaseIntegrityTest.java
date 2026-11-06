package com.ledgerline.database;

import com.ledgerline.entity.Account;
import com.ledgerline.entity.AccountPermission;
import com.ledgerline.entity.Role;
import com.ledgerline.entity.User;
import com.ledgerline.repository.AccountPermissionRepository;
import com.ledgerline.repository.AccountRepository;
import com.ledgerline.repository.RoleRepository;
import com.ledgerline.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class DatabaseIntegrityTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountPermissionRepository accountPermissionRepository;

    @Test
    @Transactional
    @DisplayName("Test 14A: Database relationship integrity between Users and Roles via composite join table")
    void shouldPersistAndRetrieveUserRolesRelationship() {
        Role role = roleRepository.findByRoleName("ROLE_ACCOUNT_HOLDER").orElseThrow();

        User user = User.builder()
                .fullName("Database Integrity User")
                .email("db.integrity@example.com")
                .passwordHash("$2a$10$xyz")
                .enabled(true)
                .roles(Set.of(role))
                .build();

        User saved = userRepository.saveAndFlush(user);

        User retrieved = userRepository.findById(saved.getId()).orElseThrow();
        assertThat(retrieved.getRoles()).hasSize(1);
        assertThat(retrieved.getRoles().iterator().next().getRoleName()).isEqualTo("ROLE_ACCOUNT_HOLDER");
    }

    @Test
    @DisplayName("Test 14B: Enforce unique constraint on account_number")
    void shouldRejectDuplicateAccountNumber() {
        String accNum = "LL-UNIQ-" + System.currentTimeMillis();

        Account acc1 = Account.builder()
                .accountNumber(accNum)
                .accountName("Primary Checking")
                .accountType("CHECKING")
                .currency("USD")
                .balance(BigDecimal.ZERO.setScale(2))
                .build();
        accountRepository.saveAndFlush(acc1);

        Account acc2 = Account.builder()
                .accountNumber(accNum)
                .accountName("Duplicate Number Account")
                .accountType("SAVINGS")
                .currency("USD")
                .balance(BigDecimal.ZERO.setScale(2))
                .build();

        assertThatThrownBy(() -> accountRepository.saveAndFlush(acc2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Transactional
    @DisplayName("Test 14C: Enforce unique constraint on (account_id, user_id) in account_permissions")
    void shouldRejectDuplicateAccountUserPermission() {
        Role role = roleRepository.findByRoleName("ROLE_ACCOUNT_HOLDER").orElseThrow();
        User user = userRepository.saveAndFlush(User.builder()
                .fullName("Perm Test User")
                .email("perm.test." + System.currentTimeMillis() + "@example.com")
                .passwordHash("$2a$10$xyz")
                .enabled(true)
                .roles(Set.of(role))
                .build());

        Account acc = accountRepository.saveAndFlush(Account.builder()
                .accountNumber("LL-PERM-" + System.currentTimeMillis())
                .accountName("Permission Test Account")
                .accountType("CHECKING")
                .currency("USD")
                .balance(BigDecimal.ZERO.setScale(2))
                .build());

        AccountPermission p1 = AccountPermission.builder()
                .account(acc)
                .user(user)
                .canView(true)
                .build();
        accountPermissionRepository.saveAndFlush(p1);

        AccountPermission p2 = AccountPermission.builder()
                .account(acc)
                .user(user)
                .canView(false)
                .build();

        assertThatThrownBy(() -> accountPermissionRepository.saveAndFlush(p2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
