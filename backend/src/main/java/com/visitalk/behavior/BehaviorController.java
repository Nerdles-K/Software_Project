package com.visitalk.behavior;

import com.visitalk.model.BehaviorEvent;
import com.visitalk.repository.BehaviorEventRepository;
import com.visitalk.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/behavior-events")
public class BehaviorController {

    private final BehaviorEventRepository repo;
    private final UserRepository users;

    public BehaviorController(BehaviorEventRepository repo, UserRepository users) {
        this.repo = repo;
        this.users = users;
    }

    /**
     * C-1: parent records an event in <= 3 clicks. Body: intensity, triggerTags[],
     * (optional) occurredAt. parentId/childId/familyId come from JWT, not the body.
     */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body, HttpServletRequest req) {
        String role = (String) req.getAttribute("role");
        Long parentId = (Long) req.getAttribute("userId");
        String familyId = (String) req.getAttribute("familyId");
        if (!"parent".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Only parent role can log behavior events"));
        }

        Object rawIntensity = body.get("intensity");
        if (!(rawIntensity instanceof Number)) {
            return ResponseEntity.badRequest().body(Map.of("error", "intensity is required (1-5)"));
        }
        int intensity = ((Number) rawIntensity).intValue();
        if (intensity < 1 || intensity > 5) {
            return ResponseEntity.badRequest().body(Map.of("error", "intensity must be 1..5"));
        }

        @SuppressWarnings("unchecked")
        List<String> tags = (List<String>) body.getOrDefault("triggerTags", List.of());
        if (tags == null) tags = List.of();
        String[] tagArr = tags.toArray(new String[0]);

        // Find the child user in the same family — privacy: never trust client-sent childId.
        Long childId = users.findAll().stream()
            .filter(u -> familyId.equals(u.getFamilyId()) && "child".equals(u.getRole()))
            .map(u -> u.getId())
            .findFirst()
            .orElse(null);
        if (childId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "No child registered in family"));
        }

        BehaviorEvent e = new BehaviorEvent();
        e.setParentId(parentId);
        e.setChildId(childId);
        e.setIntensity(intensity);
        e.setTriggerTags(tagArr);
        e.setNoteEncrypted((String) body.get("note"));
        Object occurred = body.get("occurredAt");
        e.setOccurredAt(occurred instanceof String s && !s.isBlank()
            ? LocalDateTime.parse(s)
            : LocalDateTime.now());

        return ResponseEntity.ok(repo.insert(e));
    }

    @GetMapping
    public ResponseEntity<?> list(HttpServletRequest req,
                                   @RequestParam(defaultValue = "50") int limit) {
        String role = (String) req.getAttribute("role");
        String familyId = (String) req.getAttribute("familyId");
        if (!"parent".equals(role)) {
            // Child must NEVER read behavior events (PRD §6.2 RLS rule).
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "forbidden"));
        }
        return ResponseEntity.ok(repo.findByFamilyId(familyId, Math.min(Math.max(limit, 1), 200)));
    }
}
