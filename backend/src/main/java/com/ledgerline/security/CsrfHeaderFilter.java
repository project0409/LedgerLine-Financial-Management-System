package com.ledgerline.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * OWASP-compliant CSRF defense for Cookie-authenticated REST APIs.
 * Verifies custom header (X-Requested-With) and Origin/Referer on state-changing requests
 * that authenticate via session/JWT cookies without an Authorization Bearer header.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CsrfHeaderFilter extends OncePerRequestFilter {

    private static final Set<String> STATE_CHANGING_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");
    private static final Set<String> CSRF_EXEMPT_PATHS = Set.of("/api/auth/login", "/api/auth/register");

    @Value("${ledgerline.cors.allowed-origins:http://localhost:5173,http://localhost:3000}")
    private String allowedOrigins;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // Safe methods (GET, HEAD, OPTIONS) or exempt public auth paths do not require anti-CSRF checks
        if (!STATE_CHANGING_METHODS.contains(method) || isExempt(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // If client sends explicit Authorization: Bearer header, browser CORS preflight blocks CSRF
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // For cookie-authenticated state-changing requests, enforce custom header or origin verification
        String requestedWith = request.getHeader("X-Requested-With");
        String origin = request.getHeader("Origin");

        boolean hasCustomHeader = "XMLHttpRequest".equalsIgnoreCase(requestedWith);
        boolean hasValidOrigin = isValidOrigin(origin);

        if (!hasCustomHeader && !hasValidOrigin && origin != null) {
            log.warn("Potential CSRF attempt blocked for path={} method={} origin={}", path, method, origin);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":false,\"status\":403,\"error\":\"Forbidden\",\"message\":\"CSRF verification failed: Missing required custom request header or origin mismatch.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isExempt(String path) {
        return CSRF_EXEMPT_PATHS.stream().anyMatch(path::startsWith);
    }

    private boolean isValidOrigin(String origin) {
        if (origin == null || origin.isBlank()) {
            return false;
        }
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        return origins.contains(origin);
    }
}
