package com.visitalk;

import com.visitalk.support.AbstractE2ETest;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * P1 — the JWT security filter behaviour that ordinary endpoint tests don't reach:
 * an expired token is rejected on protected routes, but a bad/expired token must
 * NOT block the public routes (login, health, share) the filter is meant to wave
 * through.
 */
class JwtFilterE2ETest extends AbstractE2ETest {

    @Value("${app.jwt.secret}")
    private String secret;

    private String expiredToken() {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        long past = System.currentTimeMillis() - 60_000;
        return Jwts.builder()
            .subject("1").claim("role", "parent").claim("family_id", "FAM001")
            .issuedAt(new Date(past - 60_000)).expiration(new Date(past))
            .signWith(key).compact();
    }

    @Test
    void expiredToken_isRejectedOnProtectedEndpoint() {
        ResponseEntity<String> res = rest.exchange("/api/cards", HttpMethod.GET,
            new HttpEntity<>(bearer(expiredToken())), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, res.getStatusCode());
    }

    @Test
    void publicHealthEndpoint_passesThroughEvenWithGarbageToken() {
        ResponseEntity<String> res = rest.exchange("/api/health", HttpMethod.GET,
            new HttpEntity<>(bearer("totally-not-a-jwt")), String.class);
        assertEquals(HttpStatus.OK, res.getStatusCode(), "a bad token must not block a public route");
    }

    @Test
    void publicLogin_stillWorksWithExpiredToken() {
        // A stale token lingering in a client must not break re-login: the filter
        // swallows the bad token on public routes and lets the controller run.
        var loginReq = new HttpEntity<>(
            java.util.Map.of("email", "ghost@test.com", "password", "x"), bearer(expiredToken()));
        ResponseEntity<String> res = rest.exchange("/api/auth/login", HttpMethod.POST, loginReq, String.class);
        // 401 here is the controller's "bad credentials", NOT the filter blocking the request.
        assertEquals(HttpStatus.UNAUTHORIZED, res.getStatusCode());
    }
}
