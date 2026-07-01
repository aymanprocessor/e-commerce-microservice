package com.raya.api_gateway.security;

import com.raya.api_gateway.util.JwtUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTests {

    private static final String TEST_SECRET =
            "361a65ca5fde3dcf658a71a7b93wedfg3b17d564a5e5cae93aac497a890978b5";

    private static final String OTHER_SECRET =
            "1111111111111111111111111111111111111111111111111111111111111111";

    private SecretKey signingKey;
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(TEST_SECRET);
        signingKey = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void isTokenValid_returnsTrue_forAValidToken() {

        String token = Jwts.builder()
                .setSubject("user1")
                .claim("role", "CUSTOMER")
                .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(signingKey)
                .compact();

        assertThat(jwtUtil.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_returnsFalse_forAnExpiredToken() {

        String token = Jwts.builder()
                .setSubject("user1")
                .claim("role", "CUSTOMER")
                .setExpiration(new Date(System.currentTimeMillis() - 1_000))
                .signWith(signingKey)
                .compact();

        assertThat(jwtUtil.isTokenValid(token)).isFalse();
    }

    @Test
    void isTokenValid_returnsFalse_forATokenSignedWithDifferentSecret() {

        String token = Jwts.builder()
                .setSubject("user1")
                .claim("role", "CUSTOMER")
                .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(signingKey)
                .compact();

        JwtUtil validator = new JwtUtil(OTHER_SECRET);

        assertThat(validator.isTokenValid(token)).isFalse();
    }
}