package com.visitalk;

import com.visitalk.model.PictogramCard;
import com.visitalk.support.AbstractE2ETest;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * P0 — the privacy / permission barrier that VisiTalk is built around
 * (CLAUDE.md "RLS Privacy", Workflow §9.2 mandatory). Two axes:
 *   1. Cross-family isolation: one family can never read or mutate another's data,
 *      even when supplying the other family's id explicitly.
 *   2. Role boundaries: child cannot touch behavior/report/alert data; parent
 *      cannot read diary content or write a diary.
 *
 * Runs on H2: every assertion here is either JPA-backed (cards, family-settings)
 * or a role/family check that returns 403/404 *before* any Postgres-only SQL.
 */
class PrivacyIsolationE2ETest extends AbstractE2ETest {

    private static final ParameterizedTypeReference<List<PictogramCard>> CARD_LIST =
        new ParameterizedTypeReference<>() {};

    // ===================== Cross-family isolation =====================

    @Test
    void cards_areScopedToOwnFamily_evenWhenQueryingAnotherFamilyId() {
        Account a = registerNewFamily("parent");
        Account b = registerNewFamily("parent");
        assertNotEquals(a.familyId(), b.familyId());

        // B explicitly tries to read A's family by query param — must still get only B's cards.
        ResponseEntity<List<PictogramCard>> res = rest.exchange(
            "/api/cards?familyId=" + a.familyId(), HttpMethod.GET,
            new HttpEntity<>(bearer(b.token())), CARD_LIST);

        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertNotNull(res.getBody());
        assertFalse(res.getBody().isEmpty());
        assertTrue(res.getBody().stream().allMatch(c -> b.familyId().equals(c.getFamilyId())),
            "JWT family must override the query param — no card from family A may leak to B");
    }

    @Test
    void parentCannotDeleteAnotherFamilysCard() {
        Account a = registerNewFamily("parent");
        Account b = registerNewFamily("parent");

        Long aCardId = firstCardId(a);

        ResponseEntity<String> del = call(HttpMethod.DELETE, "/api/cards/" + aCardId, b.token(), null, String.class);
        assertEquals(HttpStatus.NOT_FOUND, del.getStatusCode(),
            "deleting a card outside your family must look like 'not found'");

        // And A's card is untouched.
        assertTrue(rest.exchange("/api/cards", HttpMethod.GET, new HttpEntity<>(bearer(a.token())), CARD_LIST)
            .getBody().stream().anyMatch(c -> c.getId().equals(aCardId)));
    }

    @Test
    void familySettingsAreIsolatedPerFamily() {
        Account a = registerNewFamily("parent");
        Account b = registerNewFamily("parent");

        // A enables the diary feature.
        assertEquals(HttpStatus.OK, call(HttpMethod.PUT, "/api/family-settings/diary-enabled",
            a.token(), Map.of("enabled", true), String.class).getStatusCode());

        // B's settings are unaffected (still disabled by default).
        ResponseEntity<Map> bSettings = get("/api/family-settings", b.token(), Map.class);
        assertEquals(HttpStatus.OK, bSettings.getStatusCode());
        assertEquals(Boolean.FALSE, bSettings.getBody().get("diaryFeatureEnabled"));
    }

    // ===================== Role boundaries =====================

    @Test
    void child_cannotReadBehaviorEvents() {
        Account child = registerFamilyWithBothRoles()[1];
        assertEquals(HttpStatus.FORBIDDEN, get("/api/behavior-events", child.token(), String.class).getStatusCode());
    }

    @Test
    void child_cannotLogBehaviorEvent() {
        Account child = registerFamilyWithBothRoles()[1];
        ResponseEntity<String> res = call(HttpMethod.POST, "/api/behavior-events", child.token(),
            Map.of("intensity", 3, "triggerTags", List.of("Noise")), String.class);
        assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
    }

    @Test
    void child_cannotReadAlertsOrWeeklyReport() {
        Account child = registerFamilyWithBothRoles()[1];
        assertEquals(HttpStatus.FORBIDDEN, get("/api/alerts", child.token(), String.class).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, get("/api/reports/weekly", child.token(), String.class).getStatusCode());
    }

    @Test
    void child_cannotToggleDiaryFeature() {
        Account child = registerFamilyWithBothRoles()[1];
        ResponseEntity<String> res = call(HttpMethod.PUT, "/api/family-settings/diary-enabled",
            child.token(), Map.of("enabled", true), String.class);
        assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
    }

    @Test
    void child_cannotCreateCard() {
        Account child = registerFamilyWithBothRoles()[1];
        PictogramCard card = new PictogramCard();
        card.setCategory("Need");
        card.setLabelI18n("hack");
        ResponseEntity<String> res = call(HttpMethod.POST, "/api/cards", child.token(), card, String.class);
        assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
    }

    @Test
    void parent_cannotReadChildDiaryContent() {
        Account parent = registerFamilyWithBothRoles()[0];
        // GET /api/diary-entries returns raw content and is child-only.
        assertEquals(HttpStatus.FORBIDDEN, get("/api/diary-entries", parent.token(), String.class).getStatusCode());
    }

    @Test
    void parent_cannotWriteDiary() {
        Account parent = registerFamilyWithBothRoles()[0];
        ResponseEntity<String> res = call(HttpMethod.POST, "/api/diary-entries", parent.token(),
            Map.of("emotionCardId", 1), String.class);
        assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
    }

    // ---- helpers ----

    private Long firstCardId(Account a) {
        List<PictogramCard> cards = rest.exchange("/api/cards", HttpMethod.GET,
            new HttpEntity<>(bearer(a.token())), CARD_LIST).getBody();
        assertNotNull(cards);
        assertFalse(cards.isEmpty());
        return cards.get(0).getId();
    }
}
