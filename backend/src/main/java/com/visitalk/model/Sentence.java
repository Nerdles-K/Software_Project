package com.visitalk.model;

import java.time.LocalDateTime;

/**
 * PECS message in a family conversation. Either side can author one; the
 * receiver is implicitly the other role in the same family. JdbcTemplate-backed
 * because Postgres BIGINT[] cannot map cleanly via JPA without extra deps.
 */
public class Sentence {

    private Long id;
    private String familyId;
    private String senderRole; // "child" | "parent"
    private String senderName; // display name of the author (e.g. mum / dad / a child)
    private Long[] cardIds;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFamilyId() { return familyId; }
    public void setFamilyId(String familyId) { this.familyId = familyId; }
    public String getSenderRole() { return senderRole; }
    public void setSenderRole(String senderRole) { this.senderRole = senderRole; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public Long[] getCardIds() { return cardIds; }
    public void setCardIds(Long[] cardIds) { this.cardIds = cardIds; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
