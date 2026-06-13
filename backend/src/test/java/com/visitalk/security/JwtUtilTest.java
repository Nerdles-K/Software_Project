package com.visitalk.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link JwtUtil} token generation. The class is constructed
 * directly (no Spring context) and the produced token is parsed back with the
 * same signing key to verify the embedded subject/role/family_id claims and the
 * expiry honour the configured TTL.
 */
class JwtUtilTest {

    private static final String SECRET = "junit-secret-key-for-jwtutil-tests-at-least-256-bits-long-aaaa";
    private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    private Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    @Test
    void generateToken_embedsSubjectRoleAndFamilyClaims() {
        JwtUtil jwt = new JwtUtil(SECRET, 86_400_000L);

        String token = jwt.generateToken(42L, "parent", "FAM001");
        Claims claims = parse(token);

        assertEquals("42", claims.getSubject());
        assertEquals("parent", claims.get("role", String.class));
        assertEquals("FAM001", claims.get("family_id", String.class));
    }

    @Test
    void generateToken_expiryReflectsConfiguredTtl() {
        long ttlMs = 3_600_000L; // 1 hour
        JwtUtil jwt = new JwtUtil(SECRET, ttlMs);

        Claims claims = parse(jwt.generateToken(1L, "child", "FAM001"));
        long deltaMs = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();

        assertEquals(ttlMs, deltaMs, "expiry should sit exactly ttl after issued-at");
        assertTrue(claims.getExpiration().after(new Date()), "freshly minted token is not expired");
    }

    @Test
    void token_signedWithWrongKey_failsVerification() {
        JwtUtil jwt = new JwtUtil(SECRET, 86_400_000L);
        String token = jwt.generateToken(7L, "child", "FAM002");

        SecretKey wrongKey = Keys.hmacShaKeyFor(
            "a-totally-different-256-bit-secret-key-for-this-negative-test".getBytes(StandardCharsets.UTF_8));

        assertThrows(Exception.class, () ->
            Jwts.parser().verifyWith(wrongKey).build().parseSignedClaims(token));
    }
}
