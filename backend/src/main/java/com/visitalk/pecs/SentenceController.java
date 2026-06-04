package com.visitalk.pecs;

import com.visitalk.model.Sentence;
import com.visitalk.repository.SentenceRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sentences")
public class SentenceController {

    private final SentenceRepository sentences;

    public SentenceController(SentenceRepository sentences) {
        this.sentences = sentences;
    }

    /**
     * Send a sentence into the family conversation. Sender is identified by JWT
     * (role + familyId), never trusted from the request body.
     */
    @PostMapping
    public ResponseEntity<?> send(HttpServletRequest req, @RequestBody Map<String, Object> body) {
        String role = (String) req.getAttribute("role");
        String familyId = (String) req.getAttribute("familyId");
        if (role == null || familyId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "unauthenticated"));
        }
        Object raw = body.get("cardIds");
        if (!(raw instanceof List<?> list) || list.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "cardIds must be a non-empty array"));
        }
        Long[] ids = new Long[list.size()];
        for (int i = 0; i < list.size(); i++) {
            Object v = list.get(i);
            if (!(v instanceof Number n)) {
                return ResponseEntity.badRequest().body(Map.of("error", "cardIds must contain numbers"));
            }
            ids[i] = n.longValue();
        }

        Sentence s = new Sentence();
        s.setFamilyId(familyId);
        s.setSenderRole(role);
        s.setCardIds(ids);
        return ResponseEntity.ok(sentences.insert(s));
    }

    /** Conversation feed for the requester's family. */
    @GetMapping
    public ResponseEntity<?> list(HttpServletRequest req,
                                   @RequestParam(defaultValue = "100") int limit,
                                   @RequestParam(required = false) Long sinceId) {
        String familyId = (String) req.getAttribute("familyId");
        if (familyId == null) return ResponseEntity.status(401).body(Map.of("error", "unauthenticated"));
        if (sinceId != null) {
            return ResponseEntity.ok(sentences.findNewer(familyId, sinceId));
        }
        int safeLimit = Math.min(Math.max(limit, 1), 500);
        return ResponseEntity.ok(sentences.findByFamilyId(familyId, safeLimit));
    }
}
