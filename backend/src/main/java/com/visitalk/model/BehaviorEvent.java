package com.visitalk.model;

import java.time.LocalDateTime;

/**
 * Plain DTO for behavior_event rows. Backed by JdbcTemplate (not JPA) because
 * the table uses Postgres text[] for trigger_tags, which would require an extra
 * Hibernate type mapping dependency for marginal benefit.
 */
public class BehaviorEvent {

    private Long id;
    private Long parentId;
    private Long childId;
    private Integer intensity;
    private String[] triggerTags;
    private String noteEncrypted;
    private LocalDateTime occurredAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public Long getChildId() { return childId; }
    public void setChildId(Long childId) { this.childId = childId; }
    public Integer getIntensity() { return intensity; }
    public void setIntensity(Integer intensity) { this.intensity = intensity; }
    public String[] getTriggerTags() { return triggerTags; }
    public void setTriggerTags(String[] triggerTags) { this.triggerTags = triggerTags; }
    public String getNoteEncrypted() { return noteEncrypted; }
    public void setNoteEncrypted(String noteEncrypted) { this.noteEncrypted = noteEncrypted; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }
}
