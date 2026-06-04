package com.visitalk.behavior;

import com.visitalk.model.AlertDismissal;
import com.visitalk.repository.AlertDismissalRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final BehaviorAnalyticsService analytics;
    private final AlertDismissalRepository dismissals;

    public AlertController(BehaviorAnalyticsService analytics, AlertDismissalRepository dismissals) {
        this.analytics = analytics;
        this.dismissals = dismissals;
    }

    /** C-5: list trigger tags currently in a 3-day-consecutive streak and not dismissed. */
    @GetMapping
    public ResponseEntity<?> active(HttpServletRequest req) {
        if (!"parent".equals(req.getAttribute("role"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "forbidden"));
        }
        String familyId = (String) req.getAttribute("familyId");
        List<String> tags = analytics.checkConsecutiveTriggerAlert(familyId, 3);
        return ResponseEntity.ok(Map.of("alerts", tags));
    }

    @PostMapping("/dismiss")
    public ResponseEntity<?> dismiss(HttpServletRequest req,
                                     @RequestBody Map<String, String> body) {
        if (!"parent".equals(req.getAttribute("role"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "forbidden"));
        }
        String familyId = (String) req.getAttribute("familyId");
        String tag = body.get("triggerTag");
        if (tag == null || tag.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "triggerTag is required"));
        }
        AlertDismissal d = new AlertDismissal();
        d.setFamilyId(familyId);
        d.setTriggerTag(tag);
        dismissals.save(d);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
