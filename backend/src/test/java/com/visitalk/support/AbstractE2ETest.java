package com.visitalk.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Shared scaffolding for HTTP-level end-to-end tests: boots the whole app on a
 * random port (H2 in-memory DB) and exposes small helpers to register accounts
 * and call the API with a bearer token. Concrete tests subclass this.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractE2ETest {

    @Autowired
    protected TestRestTemplate rest;

    /** A registered account plus the JWT and family it was issued for. */
    public record Account(String token, String familyId, String role) {}

    protected static String uniqueEmail() {
        return "e2e-" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
    }

    /** Registers a brand-new family (no family code) and returns its account. */
    protected Account registerNewFamily(String role) {
        return register(uniqueEmail(), "secret123", role, null);
    }

    /** Registers a parent + child in the SAME new family; returns [parent, child]. */
    protected Account[] registerFamilyWithBothRoles() {
        Account parent = registerNewFamily("parent");
        Account child = register(uniqueEmail(), "secret123", "child", parent.familyId());
        return new Account[]{parent, child};
    }

    @SuppressWarnings("unchecked")
    protected Account register(String email, String password, String role, String familyCode) {
        Map<String, Object> body = familyCode == null
            ? Map.of("email", email, "password", password, "role", role)
            : Map.of("email", email, "password", password, "role", role, "familyCode", familyCode);
        ResponseEntity<Map> res = rest.postForEntity("/api/auth/register", body, Map.class);
        assertEquals(HttpStatus.OK, res.getStatusCode(), "registration should succeed for " + email);
        Map<String, Object> b = res.getBody();
        return new Account((String) b.get("token"), (String) b.get("familyId"), (String) b.get("role"));
    }

    protected HttpHeaders bearer(String token) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(token);
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    /** GET with a bearer token. */
    protected <T> ResponseEntity<T> get(String path, String token, Class<T> type) {
        return rest.exchange(path, HttpMethod.GET, new HttpEntity<>(bearer(token)), type);
    }

    /** Arbitrary method + JSON body with a bearer token. */
    protected <T> ResponseEntity<T> call(HttpMethod method, String path, String token, Object body, Class<T> type) {
        return rest.exchange(path, method, new HttpEntity<>(body, bearer(token)), type);
    }
}
