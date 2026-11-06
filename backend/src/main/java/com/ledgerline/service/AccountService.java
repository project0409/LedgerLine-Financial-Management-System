package com.ledgerline.service;

import com.ledgerline.dto.request.CreateAccountRequest;
import com.ledgerline.dto.response.AccountResponse;
import com.ledgerline.entity.Account;
import com.ledgerline.entity.AccountPermission;
import com.ledgerline.entity.User;
import com.ledgerline.exception.ResourceNotFoundException;
import com.ledgerline.mapper.AccountMapper;
import com.ledgerline.repository.AccountPermissionRepository;
import com.ledgerline.repository.AccountRepository;
import com.ledgerline.repository.UserRepository;
import com.ledgerline.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountPermissionRepository accountPermissionRepository;
    private final UserRepository userRepository;
    private final AccountMapper accountMapper;
    private final AuditLogService auditLogService;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request, CustomUserDetails userDetails, String ipAddress) {
        User creator = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userDetails.getId()));

        String accountNumber = generateUniqueAccountNumber();

        // Financial integrity: Day 1 accounts strictly initialized at 0.00 balance
        Account account = Account.builder()
                .accountNumber(accountNumber)
                .accountName(request.getAccountName().trim())
                .accountType(request.getAccountType().trim().toUpperCase())
                .currency(request.getCurrency() != null ? request.getCurrency().trim().toUpperCase() : "USD")
                .balance(BigDecimal.ZERO.setScale(2))
                .status("ACTIVE")
                .build();

        Account savedAccount = accountRepository.save(account);

        // Grant creator full permissions on the created account
        AccountPermission permission = AccountPermission.builder()
                .account(savedAccount)
                .user(creator)
                .canView(true)
                .canDeposit(true)
                .canWithdraw(true)
                .canTransfer(true)
                .grantedBy(creator)
                .build();

        accountPermissionRepository.save(permission);

        auditLogService.log(creator, "ACCOUNT_CREATION", "ACCOUNT", String.valueOf(savedAccount.getId()), ipAddress,
                "Created account " + accountNumber + " with zero initial balance");

        return accountMapper.toResponse(savedAccount);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsForUser(CustomUserDetails userDetails) {
        boolean isStaff = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_AUDITOR"));

        List<Account> accounts;
        if (isStaff) {
            accounts = accountRepository.findAll();
        } else {
            accounts = accountPermissionRepository.findPermittedAccountsByUserId(userDetails.getId());
        }

        return accounts.stream()
                .map(accountMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccountById(Long accountId, CustomUserDetails userDetails) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + accountId));

        boolean isStaff = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_AUDITOR"));

        if (!isStaff) {
            boolean hasPermission = accountPermissionRepository.findByAccountIdAndUserId(accountId, userDetails.getId())
                    .map(AccountPermission::getCanView)
                    .orElse(false);

            if (!hasPermission) {
                throw new AccessDeniedException("You do not have permission to view account id: " + accountId);
            }
        }

        return accountMapper.toResponse(account);
    }

    private String generateUniqueAccountNumber() {
        String number;
        do {
            long randomDigits = 1000000000L + (long)(RANDOM.nextDouble() * 9000000000L);
            number = "LL-" + randomDigits;
        } while (accountRepository.existsByAccountNumber(number));
        return number;
    }
}
