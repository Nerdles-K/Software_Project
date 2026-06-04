package com.visitalk.pecs;

import com.visitalk.model.FamilySettings;
import com.visitalk.repository.FamilySettingsRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/family-settings")
public class FamilySettingsController {

    private final FamilySettingsRepository repo;

    public FamilySettingsController(FamilySettingsRepository repo) {
        this.repo = repo;
    }

    /** Both roles may READ (child needs to know if the diary icon should show). */
    @GetMapping
    public ResponseEntity<?> get(HttpServletRequest req) {
        String familyId = (String) req.getAttribute("familyId");
        FamilySettings s = repo.findById(familyId).orElseGet(() -> {
            FamilySettings fresh = new FamilySettings();
            fresh.setFamilyId(familyId);
            fresh.setDiaryFeatureEnabled(false);
            return fresh;
        });
        return ResponseEntity.ok(s);
    }

    /** C-7: only parent may toggle the diary feature. */
    @PutMapping("/diary-enabled")
    public ResponseEntity<?> setDiaryEnabled(HttpServletRequest req,
                                              @RequestBody Map<String, Boolean> body) {
        if (!"parent".equals(req.getAttribute("role"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "forbidden"));
        }
        String familyId = (String) req.getAttribute("familyId");
        Boolean enabled = body.get("enabled");
        if (enabled == null) return ResponseEntity.badRequest().body(Map.of("error", "enabled is required"));

        FamilySettings s = repo.findById(familyId).orElseGet(() -> {
            FamilySettings fresh = new FamilySettings();
            fresh.setFamilyId(familyId);
            return fresh;
        });
        s.setDiaryFeatureEnabled(enabled);
        repo.save(s);
        return ResponseEntity.ok(s);
    }
}
