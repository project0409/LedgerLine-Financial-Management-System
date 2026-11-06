package com.ledgerline.controller;

import com.ledgerline.dto.request.CreateAccountRequest;
import com.ledgerline.dto.response.AccountResponse;
import com.ledgerline.dto.response.ApiResponse;
import com.ledgerline.security.CustomUserDetails;
import com.ledgerline.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Account Management", description = "Endpoints for creating and retrieving financial accounts")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @Operation(summary = "Create a new financial account initialized at zero balance")
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(
            @Valid @RequestBody CreateAccountRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {

        String ip = extractClientIp(httpRequest);
        AccountResponse response = accountService.createAccount(request, userDetails, ip);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Account created successfully with zero balance", response));
    }

    @GetMapping
    @Operation(summary = "List permitted accounts for the authenticated user (or all accounts for admin/auditor)")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAccounts(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<AccountResponse> accounts = accountService.getAccountsForUser(userDetails);
        return ResponseEntity.ok(ApiResponse.success(accounts));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retrieve specific account details by ID if authorized")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        AccountResponse account = accountService.getAccountById(id, userDetails);
        return ResponseEntity.ok(ApiResponse.success(account));
    }

    private String extractClientIp(HttpServletRequest request) {
        if (request == null) return "unknown";
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        } else {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
