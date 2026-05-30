package com.example.gamecenter.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** JWT 签发、解析与生产环境密钥强度校验。 */
@Component
public class JwtUtils {

    static final String DEFAULT_DEV_SECRET =
            "gamecenter-secret-key-2024-production-environment-please-change-this-key-to-at-least-64-bytes";

    private static final Set<String> KNOWN_WEAK_SECRETS = Set.of(
            DEFAULT_DEV_SECRET,
            "changeme",
            "secret"
    );

    private static final int MIN_PRODUCTION_SECRET_BYTES = 32;

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.expiration-time}")
    private long expirationTime;

    private final Environment environment;

    public JwtUtils(Environment environment) {
        this.environment = environment;
    }

    /** prod 环境禁止使用弱密钥或过短 secret。 */
    @PostConstruct
    void assertProductionSecretStrength() {
        if (!Arrays.asList(environment.getActiveProfiles()).contains("prod")) {
            return;
        }
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("JWT_SECRET_KEY must be set when spring.profiles.active includes prod");
        }
        if (KNOWN_WEAK_SECRETS.contains(secretKey)) {
            throw new IllegalStateException("JWT secret must not use a known weak default in production");
        }
        if (secretKey.getBytes(StandardCharsets.UTF_8).length < MIN_PRODUCTION_SECRET_BYTES) {
            throw new IllegalStateException(
                    "JWT secret must be at least " + MIN_PRODUCTION_SECRET_BYTES + " bytes in production");
        }
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String username, Long userId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey())
                .compact();
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    public Long getUserIdFromToken(String token) {
        return ((Number) getClaimsFromToken(token).get("userId")).longValue();
    }

    public String getRoleFromToken(String token) {
        return (String) getClaimsFromToken(token).get("role");
    }

    public boolean isTokenExpired(String token) {
        return getClaimsFromToken(token).getExpiration().before(new Date());
    }

    public boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}
