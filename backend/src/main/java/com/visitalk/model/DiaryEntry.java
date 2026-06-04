package com.visitalk.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "diary_entry")
public class DiaryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "child_id", nullable = false)
    private Long childId;

    @Column(name = "emotion_card_id")
    private Long emotionCardId;

    @Column(name = "doodle_url", columnDefinition = "text")
    private String doodleUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getChildId() { return childId; }
    public void setChildId(Long childId) { this.childId = childId; }
    public Long getEmotionCardId() { return emotionCardId; }
    public void setEmotionCardId(Long emotionCardId) { this.emotionCardId = emotionCardId; }
    public String getDoodleUrl() { return doodleUrl; }
    public void setDoodleUrl(String doodleUrl) { this.doodleUrl = doodleUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
