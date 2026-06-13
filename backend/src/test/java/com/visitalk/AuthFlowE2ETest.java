package com.visitalk;

import com.visitalk.model.PictogramCard;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end test: boots the entire application on a random port (H2 in-memory DB
 * via src/test/resources/application.yml) and drives it over real HTTP through the
 * security filter chain. Covers the full account lifecycle —
 * register → login → access a JWT-protected endpoint — plus the auth rejections
 * that protect every non-public route.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthFlowE2ETest {

    @Autowired
    private TestRestTemplate rest;

    private static String uniqueEmail() {
        return "e2e-" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
    }

    @Test
    void registerThenLogin_thenAccessProtectedCardsWithJwt() {
        String email = uniqueEmail();

        // 1) Register a brand-new parent (creates a fresh family + seeds its card library).
        ResponseEntity<Map> reg = rest.postForEntity("/api/auth/register",
            Map.of("email", email, "password", "secret123", "role", "parent"), Map.class);
        assertEquals(HttpStatus.OK, reg.getStatusCode());
        String familyId = (String) reg.getBody().get("familyId");
        assertNotNull(familyId);
        assertEquals("parent", reg.getBody().get("role"));

        // 2) Log in with the same credentials and pick up a fresh token.
        ResponseEntity<Map> login = rest.postForEntity("/api/auth/login",
            Map.of("email", email, "password", "secret123"), Map.class);
        assertEquals(HttpStatus.OK, login.getStatusCode());
        String token = (String) login.getBody().get("token");
        assertNotNull(token);
        assertEquals(familyId, login.getBody().get("familyId"));

        // 3) Use the token to hit a protected endpoint; the seeded default library is returned.
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<List<PictogramCard>> cards = rest.exchange(
            "/api/cards?familyId=" + familyId, HttpMethod.GET,
            new HttpEntity<>(headers), new ParameterizedTypeReference<>() {});

        assertEquals(HttpStatus.OK, cards.getStatusCode());
        assertNotNull(cards.getBody());
        assertFalse(cards.getBody().isEmpty(), "a new family should start with the default card library");
    }

    @Test
    void login_wrongPassword_returns401() {
        String email = uniqueEmail();
        rest.postForEntity("/api/auth/register",
            Map.of("email", email, "password", "secret123", "role", "parent"), Map.class);

        ResponseEntity<Map> login = rest.postForEntity("/api/auth/login",
            Map.of("email", email, "password", "WRONG"), Map.class);

        assertEquals(HttpStatus.UNAUTHORIZED, login.getStatusCode());
    }

    @Test
    void register_duplicateEmail_returns400() {
        String email = uniqueEmail();
        Map<String, String> body = Map.of("email", email, "password", "secret123", "role", "parent");

        assertEquals(HttpStatus.OK, rest.postForEntity("/api/auth/register", body, Map.class).getStatusCode());
        // Second registration with the same email is rejected by the service guard.
        assertEquals(HttpStatus.BAD_REQUEST,
            rest.postForEntity("/api/auth/register", body, Map.class).getStatusCode());
    }

    @Test
    void protectedEndpoint_withoutToken_isRejected() {
        ResponseEntity<String> res = rest.getForEntity("/api/cards?familyId=FAM001", String.class);
        assertTrue(res.getStatusCode().is4xxClientError(),
            "no JWT should be blocked by the security filter chain, got " + res.getStatusCode());
    }

    @Test
    void protectedEndpoint_withGarbageToken_returns401() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("not-a-real-jwt");
        ResponseEntity<String> res = rest.exchange("/api/cards?familyId=FAM001", HttpMethod.GET,
            new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, res.getStatusCode());
    }
}
