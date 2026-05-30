package com.example.gamecenter.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        org.springframework.core.env.Environment environment =
                org.mockito.Mockito.mock(org.springframework.core.env.Environment.class);
        org.mockito.Mockito.when(environment.getActiveProfiles()).thenReturn(new String[0]);

        jwtUtils = new JwtUtils(environment);
        ReflectionTestUtils.setField(jwtUtils, "secretKey", 
            JwtUtils.DEFAULT_DEV_SECRET);
        ReflectionTestUtils.setField(jwtUtils, "expirationTime", 86400000L);
    }

    @Test
    void testGenerateToken() {
        String token = jwtUtils.generateToken("testuser", 1L, "user");
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testValidateToken() {
        String token = jwtUtils.generateToken("testuser", 1L, "user");
        
        assertTrue(jwtUtils.validateToken(token));
    }

    @Test
    void testValidateInvalidToken() {
        assertFalse(jwtUtils.validateToken("invalid-token"));
        assertFalse(jwtUtils.validateToken(""));
        assertFalse(jwtUtils.validateToken(null));
    }

    @Test
    void testGetUsernameFromToken() {
        String token = jwtUtils.generateToken("testuser", 1L, "user");
        
        assertEquals("testuser", jwtUtils.getUsernameFromToken(token));
    }

    @Test
    void testGetUserIdFromToken() {
        String token = jwtUtils.generateToken("testuser", 123L, "user");
        
        assertEquals(123L, jwtUtils.getUserIdFromToken(token));
    }

    @Test
    void testGetRoleFromToken() {
        String token = jwtUtils.generateToken("testuser", 1L, "admin");
        
        assertEquals("admin", jwtUtils.getRoleFromToken(token));
    }

    @Test
    void testTokenNotExpired() {
        String token = jwtUtils.generateToken("testuser", 1L, "user");
        
        assertFalse(jwtUtils.isTokenExpired(token));
    }
}
