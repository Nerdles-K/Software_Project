package com.visitalk.model;

import java.time.LocalDate;

/**
 * Per-day run of a template. `completedStepIndices` stores 0-based indices into
 * the parent template's `steps` array (NOT card IDs), so duplicate cards within
 * one schedule still resolve unambiguously.
 */
public class ScheduleInstance {

    private Long id;
    private Long templateId;
    private LocalDate date;
    private Long[] completedStepIndices;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public Long[] getCompletedStepIndices() { return completedStepIndices; }
    public void setCompletedStepIndices(Long[] completedStepIndices) { this.completedStepIndices = completedStepIndices; }
}
