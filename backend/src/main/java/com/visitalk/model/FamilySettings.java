package com.visitalk.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity
@Table(name = "system_configs")
public class FamilySettings {

    @Id
    @Column(name = "family_id")
    private String familyId;

    @Column(name = "diary_feature_enabled")
    @JsonProperty("diaryFeatureEnabled")
    private boolean diaryFeatureEnabled = false;

    public String getFamilyId() { return familyId; }
    public void setFamilyId(String familyId) { this.familyId = familyId; }

    @JsonProperty("diaryFeatureEnabled")
    public boolean isDiaryFeatureEnabled() { return diaryFeatureEnabled; }

    @JsonProperty("diaryFeatureEnabled")
    public void setDiaryFeatureEnabled(boolean diaryFeatureEnabled) {
        this.diaryFeatureEnabled = diaryFeatureEnabled;
    }
}
