package com.visitalk.pecs;

import com.visitalk.model.DiaryEntry;
import com.visitalk.model.FamilySettings;
import com.visitalk.repository.DiaryEntryRepository;
import com.visitalk.repository.FamilySettingsRepository;
import com.visitalk.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/diary-entries")
public class DiaryController {

    private final DiaryEntryRepository diary;
    private final FamilySettingsRepository settings;
    private final UserRepository users;

    public DiaryController(DiaryEntryRepository diary,
                            FamilySettingsRepository settings,
                            UserRepository users) {
        this.diary = diary;
        this.settings = settings;
        this.users = users;
    }

    /** C-6: child writes a diary entry. Only child role can call this. */
    @PostMapping
    public ResponseEntity<?> create(HttpServletRequest req, @RequestBody DiaryEntry body) {
        String role = (String) req.getAttribute("role");
        Long childId = (Long) req.getAttribute("userId");
        String familyId = (String) req.getAttribute("familyId");
        if (!"child".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Only child can write diary"));
        }
        if (!isDiaryEnabled(familyId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Diary feature is disabled"));
        }
        if (body.getEmotionCardId() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "emotionCardId is required"));
        }
        body.setChildId(childId);
        body.setCreatedAt(LocalDateTime.now());
        return ResponseEntity.ok(diary.save(body));
    }

    /**
     * C-6: child reads their own diary entries (content visible).
     * Parents CANNOT use this endpoint — they have /check-today only.
     */
    @GetMapping
    public ResponseEntity<?> listOwn(HttpServletRequest req) {
        String role = (String) req.getAttribute("role");
        Long childId = (Long) req.getAttribute("userId");
        if (!"child".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Diary content is private to the child"));
        }
        return ResponseEntity.ok(diary.findByChildIdOrderByCreatedAtDesc(childId));
    }

    /**
     * C-7: parent ONLY sees a boolean — has the child written a diary today?
     * Content fields (emotion_card_id, doodle_url) are NEVER returned.
     */
    @GetMapping("/check-today")
    public ResponseEntity<?> checkToday(HttpServletRequest req) {
        String role = (String) req.getAttribute("role");
        String familyId = (String) req.getAttribute("familyId");
        if (!"parent".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "forbidden"));
        }
        if (!isDiaryEnabled(familyId)) {
            return ResponseEntity.ok(Map.of("enabled", false, "writtenToday", false));
        }
        Long childId = users.findAll().stream()
            .filter(u -> familyId.equals(u.getFamilyId()) && "child".equals(u.getRole()))
            .map(u -> u.getId())
            .findFirst()
            .orElse(null);
        if (childId == null) return ResponseEntity.ok(Map.of("enabled", true, "writtenToday", false));

        LocalDate today = LocalDate.now();
        List<DiaryEntry> entries = diary.findByChildIdAndDay(
            childId, today.atStartOfDay(), today.plusDays(1).atStartOfDay());
        return ResponseEntity.ok(Map.of(
            "enabled", true,
            "writtenToday", !entries.isEmpty(),
            "count", entries.size()
        ));
    }

    private boolean isDiaryEnabled(String familyId) {
        return settings.findById(familyId)
            .map(FamilySettings::isDiaryFeatureEnabled)
            .orElse(false);
    }
}
