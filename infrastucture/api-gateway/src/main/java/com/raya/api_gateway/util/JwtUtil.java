package com.raya.api_gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

import static java.nio.charset.StandardCharsets.UTF_8;

@Component
public class JwtUtil {
    private final String secret;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.secret = secret;
        System.out.println("JWT Secret = [" + secret + "]");
    }
    private  SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(UTF_8));
    }

    public Claims validateToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody(); // throws if invalid or expired
    }

    public boolean isTokenValid(String token){
        try {
            validateToken(token);
            return true;
        } catch (JwtException e){
            return false;
        }
    }

}
