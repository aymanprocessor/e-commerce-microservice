package com.raya.api_gateway.security;

import com.raya.api_gateway.util.JwtUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class JwtUtilTests {
    private static final String TEST_SECRET =
            "361a65ca5fde3dcf658a71a7b93wedfg3b17d564a5e5cae93aac497a890978b5";

    @InjectMocks
    private JwtUtil jwtUtil;

    private SecretKey signingKey;


    @BeforeEach
    void setUp() {

        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
        signingKey = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void isTokenValid_returnsTrue_forAValidToken() {
        String token = Jwts.builder()
                .setSubject("user1")
                .claim("role", "CUSTOMER")
                .setExpiration(new Date(System.currentTimeMillis() + 3_600_000)) // 1 hour
                .signWith(signingKey)
                .compact();

        assertThat(jwtUtil.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_returnsFalse_forAnExpiredToken() {
        String token = Jwts.builder()
                .setSubject("user1")
                .claim("role", "CUSTOMER")
                .setExpiration(new Date(System.currentTimeMillis() - 1_000)) // already in the past
                .signWith(signingKey)
                .compact();

        assertThat(jwtUtil.isTokenValid(token)).isFalse();
    }

    @Test
    void isTokenValid_returnsFalse_forATamperedToken() {
        String token = Jwts.builder()
                .setSubject("user1")
                .claim("role", "CUSTOMER")
                .setExpiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(signingKey)
                .compact();

        String tampered = token.substring(0, token.length() - 1)
                + (token.endsWith("a") ? "b" : "a"); // flip the last signature character

        assertThat(jwtUtil.isTokenValid(tampered)).isFalse();
    }
}
