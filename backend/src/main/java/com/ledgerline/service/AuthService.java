package com.ledgerline.service;

import com.ledgerline.dto.request.LoginRequest;
import com.ledgerline.dto.request.RegisterRequest;
import com.ledgerline.dto.response.AuthResponse;
import com.ledgerline.dto.response.UserResponse;
import com.ledgerline.entity.Role;
import com.ledgerline.entity.User;
import com.ledgerline.exception.BadRequestException;
import com.ledgerline.exception.ConflictException;
import com.ledgerline.exception.ResourceNotFoundException;
import com.ledgerline.mapper.UserMapper;
import com.ledgerline.repository.RoleRepository;
import com.ledgerline.repository.UserRepository;
import com.ledgerline.security.CustomUserDetails;
import com.ledgerline.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;
    private final AuditLogService auditLogService;

    @Transactional
    public UserResponse register(RegisterRequest request, HttpServletRequest httpRequest) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Password and confirmation password do not match");
        }

        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Email is already registered: " + email);
        }

        Role accountHolderRole = roleRepository.findByRoleName("ROLE_ACCOUNT_HOLDER")
                .orElseThrow(() -> new ResourceNotFoundException("Default role ROLE_ACCOUNT_HOLDER not found"));

        Set<Role> roles = new HashSet<>();
        roles.add(accountHolderRole);

        User user = User.builder()
                .fullName(request.getFullName().trim())
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber() != null ? request.getPhoneNumber().trim() : null)
                .enabled(true)
                .roles(roles)
                .build();

        User savedUser = userRepository.saveAndFlush(user);

        String ip = extractClientIp(httpRequest);
        auditLogService.log(savedUser, "USER_REGISTRATION", "USER", String.valueOf(savedUser.getId()), ip,
                "New user registered with default role ROLE_ACCOUNT_HOLDER");

        return userMapper.toResponse(savedUser);
    }

    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String email = request.getEmail().trim().toLowerCase();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        String jwt = jwtTokenProvider.generateToken(authentication);

        String ip = extractClientIp(httpRequest);
        auditLogService.log(user, "USER_LOGIN", "AUTH", String.valueOf(user.getId()), ip,
                "User successfully authenticated via credentials");

        return AuthResponse.builder()
                .token(jwt)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationMs())
                .user(userMapper.toResponse(user))
                .build();
    }

    public ResponseCookie createAuthCookie(String token) {
        return jwtTokenProvider.createJwtCookie(token);
    }

    public ResponseCookie clearAuthCookie() {
        return jwtTokenProvider.createCleanJwtCookie();
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUserResponse(CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new BadRequestException("No authenticated user found");
        }
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found in database"));
        return userMapper.toResponse(user);
    }

    public void logLogout(CustomUserDetails userDetails, HttpServletRequest httpRequest) {
        if (userDetails != null) {
            String ip = extractClientIp(httpRequest);
            auditLogService.log(userDetails.getUser(), "USER_LOGOUT", "AUTH",
                    String.valueOf(userDetails.getId()), ip, "User logged out");
        }
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
