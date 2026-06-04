package com.visitalk.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "report_share")
public class ReportShare {

    @Id
    private String token;

    @Column(name = "family_id", nullable = false)
    private String familyId;

    @Column(name = "week_start", nullable = false)
    private java.time.LocalDate weekStart;

    @Column(name = "payload_json", columnDefinition = "text", nullable = false)
    private String payloadJson;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getFamilyId() { return familyId; }
    public void setFamilyId(String familyId) { this.familyId = familyId; }
    public java.time.LocalDate getWeekStart() { return weekStart; }
    public void setWeekStart(java.time.LocalDate weekStart) { this.weekStart = weekStart; }
    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}
