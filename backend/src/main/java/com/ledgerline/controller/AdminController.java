package com.ledgerline.controller;

import com.ledgerline.dto.response.ApiResponse;
import com.ledgerline.dto.response.RoleResponse;
import com.ledgerline.dto.response.UserResponse;
import com.ledgerline.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "Administration", description = "Privileged administrator operations for user and role governance")
public class AdminController {

    private final UserService userService;

    @GetMapping("/users")
    @Operation(summary = "List all registered users (Admin only)")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/roles")
    @Operation(summary = "List all available security roles (Admin only)")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
        List<RoleResponse> roles = userService.getAllRoles();
        return ResponseEntity.ok(ApiResponse.success(roles));
    }
}
