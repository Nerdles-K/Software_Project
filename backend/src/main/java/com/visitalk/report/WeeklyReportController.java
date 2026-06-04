package com.visitalk.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.visitalk.behavior.BehaviorAnalyticsService;
import com.visitalk.dto.WeeklyReportResponse;
import com.visitalk.model.ReportShare;
import com.visitalk.repository.ReportShareRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
public class WeeklyReportController {

    private final BehaviorAnalyticsService analytics;
    private final ReportShareRepository shares;
    private final ObjectMapper json;

    public WeeklyReportController(BehaviorAnalyticsService analytics,
                                   ReportShareRepository shares,
                                   ObjectMapper json) {
        this.analytics = analytics;
        this.shares = shares;
        this.json = json;
    }

    @GetMapping("/weekly")
    public ResponseEntity<?> weekly(HttpServletRequest req,
                                     @RequestParam(required = false) String date) {
        String role = (String) req.getAttribute("role");
        if (!"parent".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "forbidden"));
        }
        String familyId = (String) req.getAttribute("familyId");
        LocalDate target = (date == null || date.isBlank()) ? LocalDate.now() : LocalDate.parse(date);
        return ResponseEntity.ok(analytics.generateWeeklyReportData(familyId, target));
    }

    /** C-4: create a 24h-expiring share token snapshotting the current week's report. */
    @PostMapping("/share")
    public ResponseEntity<?> share(HttpServletRequest req,
                                    @RequestBody(required = false) Map<String, Object> body) {
        String role = (String) req.getAttribute("role");
        if (!"parent".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "forbidden"));
        }
        String familyId = (String) req.getAttribute("familyId");
        Object dateRaw = body == null ? null : body.get("date");
        LocalDate target = (dateRaw instanceof String s && !s.isBlank()) ? LocalDate.parse(s) : LocalDate.now();
        WeeklyReportResponse report = analytics.generateWeeklyReportData(familyId, target);

        String token = UUID.randomUUID().toString().replace("-", "");
        ReportShare share = new ReportShare();
        share.setToken(token);
        share.setFamilyId(familyId);
        share.setWeekStart(report.getWeekStartDate());
        try {
            share.setPayloadJson(json.writeValueAsString(report));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "serialize failed"));
        }
        share.setExpiresAt(LocalDateTime.now().plusHours(24));
        shares.save(share);

        return ResponseEntity.ok(Map.of(
            "token", token,
            "expiresAt", share.getExpiresAt().toString()
        ));
    }
}
