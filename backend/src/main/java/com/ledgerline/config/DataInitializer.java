package com.ledgerline.config;

import com.ledgerline.entity.Role;
import com.ledgerline.entity.User;
import com.ledgerline.repository.RoleRepository;
import com.ledgerline.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Verifying initial security roles and default administrative users...");

        Role adminRole = findOrCreateRole("ROLE_ADMIN", "System Administrator with user management and governance permissions");
        Role holderRole = findOrCreateRole("ROLE_ACCOUNT_HOLDER", "Account Holder with access to own accounts and authorized operations");
        Role auditorRole = findOrCreateRole("ROLE_AUDITOR", "Auditor with read-only permissions across ledger and audit logs");

        // Seed default Admin if not present
        if (!userRepository.existsByEmailIgnoreCase("admin@ledgerline.com")) {
            User admin = User.builder()
                    .fullName("System Administrator")
                    .email("admin@ledgerline.com")
                    .passwordHash(passwordEncoder.encode("Admin@12345"))
                    .phoneNumber("+1-555-0100")
                    .enabled(true)
                    .roles(Set.of(adminRole))
                    .build();
            userRepository.save(admin);
            log.info("Created default administrator: admin@ledgerline.com");
        }

        // Seed default Auditor if not present
        if (!userRepository.existsByEmailIgnoreCase("auditor@ledgerline.com")) {
            User auditor = User.builder()
                    .fullName("Chief Auditor")
                    .email("auditor@ledgerline.com")
                    .passwordHash(passwordEncoder.encode("Auditor@12345"))
                    .phoneNumber("+1-555-0101")
                    .enabled(true)
                    .roles(Set.of(auditorRole))
                    .build();
            userRepository.save(auditor);
            log.info("Created default auditor: auditor@ledgerline.com");
        }
    }

    private Role findOrCreateRole(String name, String description) {
        return roleRepository.findByRoleName(name)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .roleName(name)
                        .description(description)
                        .build()));
    }
}
