package com.visitalk.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.visitalk.dto.WeeklyReportResponse;
import com.visitalk.repository.ReportShareRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * C-4: anonymous read-only access. Token must be valid and unexpired.
 * No auth header required; mapped to /share/** which SecurityConfig permits all.
 */
@RestController
@RequestMapping("/share")
public class PublicShareController {

    private final ReportShareRepository shares;
    private final ObjectMapper json;

    public PublicShareController(ReportShareRepository shares, ObjectMapper json) {
        this.shares = shares;
        this.json = json;
    }

    @GetMapping("/reports/{token}")
    public ResponseEntity<?> read(@PathVariable String token) {
        return shares.findById(token)
            .map(s -> {
                if (s.getExpiresAt().isBefore(LocalDateTime.now())) {
                    return ResponseEntity.status(HttpStatus.GONE)
                        .body((Object) Map.of("error", "Share link expired"));
                }
                try {
                    WeeklyReportResponse payload = json.readValue(s.getPayloadJson(), WeeklyReportResponse.class);
                    return ResponseEntity.ok((Object) payload);
                } catch (Exception e) {
                    return ResponseEntity.internalServerError()
                        .body((Object) Map.of("error", "payload corrupt"));
                }
            })
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Share link not found")));
    }
}
