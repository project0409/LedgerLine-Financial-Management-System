package com.ledgerline.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final long expirationMs = 3600000;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", secret);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", expirationMs);
        ReflectionTestUtils.setField(jwtTokenProvider, "cookieName", "ledgerline_jwt");
        jwtTokenProvider.init();
    }

    @Test
    @DisplayName("Test 6: Successful JWT validation and claims extraction")
    void shouldGenerateAndValidateTokenSuccessfully() {
        String token = jwtTokenProvider.generateTokenFromUser("john.doe@example.com", 101L, List.of("ROLE_ACCOUNT_HOLDER"));

        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUsernameFromJwt(token)).isEqualTo("john.doe@example.com");
        assertThat(jwtTokenProvider.getUserIdFromJwt(token)).isEqualTo(101L);
    }

    @Test
    @DisplayName("Test 7: Expired JWT rejection")
    void shouldRejectExpiredToken() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);

        Date past = new Date(System.currentTimeMillis() - 100000);
        String expiredToken = Jwts.builder()
                .subject("expired@example.com")
                .claim("userId", 999L)
                .issuedAt(new Date(past.getTime() - 10000))
                .expiration(past)
                .signWith(key)
                .compact();

        assertThat(jwtTokenProvider.validateToken(expiredToken)).isFalse();
    }

    @Test
    @DisplayName("Test JWT rejection on tampered token")
    void shouldRejectTamperedToken() {
        String token = jwtTokenProvider.generateTokenFromUser("valid@example.com", 1L, List.of("ROLE_ACCOUNT_HOLDER"));
        String tamperedToken = token.substring(0, token.length() - 5) + "abcde";

        assertThat(jwtTokenProvider.validateToken(tamperedToken)).isFalse();
    }
}
