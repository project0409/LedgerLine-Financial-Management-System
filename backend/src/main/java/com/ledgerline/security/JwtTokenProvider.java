package com.ledgerline.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${ledgerline.security.jwt.secret:}")
    private String jwtSecret;

    @Value("${ledgerline.security.jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    @Value("${ledgerline.security.jwt.cookie-name:ledgerline_jwt}")
    private String cookieName;

    @Value("${ledgerline.security.jwt.cookie-secure:false}")
    private boolean cookieSecure;

    private SecretKey key;

    @PostConstruct
    public void init() {
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            throw new IllegalStateException("FATAL SECURITY CONFIGURATION: JWT_SECRET environment variable is missing. A strong 256-bit (32+ character) secret is strictly required.");
        }

        if (jwtSecret.trim().length() < 32) {
            throw new IllegalStateException("FATAL SECURITY CONFIGURATION: JWT_SECRET is too short. It must be at least 32 characters (256 bits) to ensure cryptographic safety.");
        }

        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(jwtSecret);
        } catch (IllegalArgumentException e) {
            keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        }
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, Math.min(keyBytes.length, 32));
            keyBytes = padded;
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return generateTokenFromUser(userDetails.getUsername(), userDetails.getId(),
                userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()));
    }

    public String generateTokenFromUser(String email, Long userId, List<String> roles) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public String getUsernameFromJwt(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    public Long getUserIdFromJwt(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("userId", Long.class);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Malformed JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    public long getExpirationMs() {
        return jwtExpirationMs;
    }

    public String extractToken(HttpServletRequest request) {
        // 1. Try Authorization header
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        // 2. Try HttpOnly Cookie
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookieName.equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank()) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    public ResponseCookie createJwtCookie(String token) {
        return ResponseCookie.from(cookieName, token)
                .path("/")
                .maxAge(jwtExpirationMs / 1000)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .build();
    }

    public ResponseCookie createCleanJwtCookie() {
        return ResponseCookie.from(cookieName, "")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .build();
    }
}
