package com.visitalk.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity
@Table(name = "pictogram_card")
public class PictogramCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "family_id", nullable = false)
    private String familyId;

    @Column(nullable = false)
    private String category;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "label_i18n")
    private String labelI18n;

    @Column(name = "is_custom")
    @JsonProperty("isCustom")
    private boolean isCustom;

    @Column(name = "sort_order")
    private int sortOrder;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFamilyId() { return familyId; }
    public void setFamilyId(String familyId) { this.familyId = familyId; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getLabelI18n() { return labelI18n; }
    public void setLabelI18n(String labelI18n) { this.labelI18n = labelI18n; }
    @JsonProperty("isCustom")
    public boolean isCustom() { return isCustom; }
    @JsonProperty("isCustom")
    public void setCustom(boolean custom) { isCustom = custom; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}
