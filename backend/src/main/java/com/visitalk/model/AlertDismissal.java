package com.visitalk.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * One row per (family, trigger_tag, cycle) means the parent dismissed the
 * "3 consecutive days" alert for that tag. We re-fire only when a NEW
 * consecutive run starts after this dismissal.
 */
@Entity
@Table(name = "alert_dismissal")
public class AlertDismissal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "family_id", nullable = false)
    private String familyId;

    @Column(name = "trigger_tag", nullable = false)
    private String triggerTag;

    @Column(name = "dismissed_at", nullable = false)
    private LocalDateTime dismissedAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFamilyId() { return familyId; }
    public void setFamilyId(String familyId) { this.familyId = familyId; }
    public String getTriggerTag() { return triggerTag; }
    public void setTriggerTag(String triggerTag) { this.triggerTag = triggerTag; }
    public LocalDateTime getDismissedAt() { return dismissedAt; }
    public void setDismissedAt(LocalDateTime dismissedAt) { this.dismissedAt = dismissedAt; }
}
