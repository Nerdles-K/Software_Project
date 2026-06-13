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
 * P1 — endpoint behaviour over real HTTP for the JPA-backed features
 * (cards, diary, family-settings) plus the validation / role guards that the
 * Postgres-array features (behavior, schedules) run *before* touching the DB.
 *
 * The array-backed happy paths (insert into behavior_event / schedule_template)
 * use Postgres `text[]` / `bigint[]` and are out of scope on H2 — see the test
 * report §6; those are covered at the unit level instead.
 */
class ApiEndpointE2ETest extends AbstractE2ETest {

    private static final ParameterizedTypeReference<List<PictogramCard>> CARD_LIST =
        new ParameterizedTypeReference<>() {};

    // ===================== Cards CRUD =====================

    @Test
    void parentCanCreateRenameAndDeleteCard() {
        Account parent = registerNewFamily("parent");

        PictogramCard card = new PictogramCard();
        card.setCategory("Need");
        card.setLabelI18n("Snack");
        card.setImageUrl("emoji:🍪");
        ResponseEntity<PictogramCard> created = call(HttpMethod.POST, "/api/cards", parent.token(), card, PictogramCard.class);
        assertEquals(HttpStatus.OK, created.getStatusCode());
        Long id = created.getBody().getId();
        assertNotNull(id);
        assertEquals(parent.familyId(), created.getBody().getFamilyId(), "new card is forced into caller's family");

        // Rename via PATCH.
        ResponseEntity<PictogramCard> renamed = call(HttpMethod.PATCH, "/api/cards/" + id, parent.token(),
            Map.of("labelI18n", "Cookie"), PictogramCard.class);
        assertEquals(HttpStatus.OK, renamed.getStatusCode());
        assertEquals("Cookie", renamed.getBody().getLabelI18n());

        // Delete.
        assertEquals(HttpStatus.OK, call(HttpMethod.DELETE, "/api/cards/" + id, parent.token(), null, String.class).getStatusCode());
        boolean stillThere = rest.exchange("/api/cards", HttpMethod.GET, new HttpEntity<>(bearer(parent.token())), CARD_LIST)
            .getBody().stream().anyMatch(c -> c.getId().equals(id));
        assertFalse(stillThere, "deleted card should be gone from the family library");
    }

    // ===================== Diary flow + privacy =====================

    @Test
    void diaryFlow_childWritesAndReads_parentSeesOnlyBoolean() {
        Account[] fam = registerFamilyWithBothRoles();
        Account parent = fam[0], child = fam[1];

        // Parent enables the diary feature.
        assertEquals(HttpStatus.OK, call(HttpMethod.PUT, "/api/family-settings/diary-enabled",
            parent.token(), Map.of("enabled", true), String.class).getStatusCode());

        // Child writes an entry referencing a real card id from the family library.
        Long cardId = rest.exchange("/api/cards", HttpMethod.GET, new HttpEntity<>(bearer(child.token())), CARD_LIST)
            .getBody().get(0).getId();
        ResponseEntity<Map> write = call(HttpMethod.POST, "/api/diary-entries", child.token(),
            Map.of("emotionCardId", cardId), Map.class);
        assertEquals(HttpStatus.OK, write.getStatusCode());

        // Child reads their own entries — content (emotionCardId) is visible to them.
        ResponseEntity<List> own = get("/api/diary-entries", child.token(), List.class);
        assertEquals(HttpStatus.OK, own.getStatusCode());
        assertFalse(own.getBody().isEmpty());

        // Parent's check-today returns ONLY a boolean + count — never the content fields.
        ResponseEntity<Map> check = get("/api/diary-entries/check-today", parent.token(), Map.class);
        assertEquals(HttpStatus.OK, check.getStatusCode());
        assertEquals(Boolean.TRUE, check.getBody().get("writtenToday"));
        assertEquals(1, ((Number) check.getBody().get("count")).intValue());
        assertFalse(check.getBody().containsKey("emotionCardId"), "parent must not see diary content");
        assertFalse(check.getBody().containsKey("doodleUrl"), "parent must not see diary content");
    }

    @Test
    void diaryWrite_isRejectedWhenFeatureDisabled() {
        Account child = registerFamilyWithBothRoles()[1]; // feature defaults to disabled
        ResponseEntity<String> res = call(HttpMethod.POST, "/api/diary-entries", child.token(),
            Map.of("emotionCardId", 1), String.class);
        assertEquals(HttpStatus.CONFLICT, res.getStatusCode());
    }

    // ===================== Family settings =====================

    @Test
    void familySettings_defaultDisabled_thenParentEnables() {
        Account parent = registerNewFamily("parent");

        ResponseEntity<Map> def = get("/api/family-settings", parent.token(), Map.class);
        assertEquals(Boolean.FALSE, def.getBody().get("diaryFeatureEnabled"));

        call(HttpMethod.PUT, "/api/family-settings/diary-enabled", parent.token(), Map.of("enabled", true), String.class);

        ResponseEntity<Map> after = get("/api/family-settings", parent.token(), Map.class);
        assertEquals(Boolean.TRUE, after.getBody().get("diaryFeatureEnabled"));
    }

    // ===================== Validation guards (return before array SQL) =====================

    @Test
    void behaviorEvent_invalidIntensity_returns400() {
        Account parent = registerFamilyWithBothRoles()[0];
        assertEquals(HttpStatus.BAD_REQUEST, call(HttpMethod.POST, "/api/behavior-events", parent.token(),
            Map.of("intensity", 9, "triggerTags", List.of("Noise")), String.class).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, call(HttpMethod.POST, "/api/behavior-events", parent.token(),
            Map.of("triggerTags", List.of("Noise")), String.class).getStatusCode());
    }

    @Test
    void scheduleTemplate_validationAndRoleGuards() {
        Account[] fam = registerFamilyWithBothRoles();
        Account parent = fam[0], child = fam[1];

        // Child may not create a schedule template.
        assertEquals(HttpStatus.FORBIDDEN, call(HttpMethod.POST, "/api/schedules/templates", child.token(),
            Map.of("name", "Morning", "steps", List.of(1, 2)), String.class).getStatusCode());

        // Parent with invalid payloads — all rejected before any DB write.
        assertEquals(HttpStatus.BAD_REQUEST, call(HttpMethod.POST, "/api/schedules/templates", parent.token(),
            Map.of("name", "", "steps", List.of(1)), String.class).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, call(HttpMethod.POST, "/api/schedules/templates", parent.token(),
            Map.of("name", "Empty", "steps", List.of()), String.class).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, call(HttpMethod.POST, "/api/schedules/templates", parent.token(),
            Map.of("name", "TooMany", "steps", List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)), String.class).getStatusCode());
    }
}
