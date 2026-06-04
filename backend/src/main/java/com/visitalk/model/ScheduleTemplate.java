package com.visitalk.model;

import java.time.LocalDateTime;

/**
 * Plain DTO for schedule_template rows. Uses JdbcTemplate (not JPA) because
 * the table relies on Postgres BIGINT[] which JPA cannot map natively.
 */
public class ScheduleTemplate {

    private Long id;
    private String familyId;
    private String name;
    private Long[] steps;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFamilyId() { return familyId; }
    public void setFamilyId(String familyId) { this.familyId = familyId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long[] getSteps() { return steps; }
    public void setSteps(Long[] steps) { this.steps = steps; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
