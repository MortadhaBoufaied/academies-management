package com.footballacademy.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "conversations", indexes = {
    @Index(name = "idx_conversation_academy", columnList = "academy_id"),
    @Index(name = "idx_conversation_division", columnList = "division_id")
})
public
class Conversation {
    public
    enum ConversationType {
        DIRECT, GROUP, DIVISION
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ElementCollection
    @CollectionTable(name = "conversation_participants", joinColumns =
    @JoinColumn(name = "conversation_id"))
    @Column(name = "participant_id", nullable = false)
    private List<Long> participantIds = new ArrayList<>();
    @Column
    private String title;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConversationType type;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academy_id", foreignKey =
    @ForeignKey(name = "fk_conversation_academy"))
    private Academy academy;
    @Column(name = "division_id")
    private Long divisionId;
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    public Conversation() {
        // JPA
    }
    public static Conversation createDirect(Long userA, Long userB) {
        Conversation c = new Conversation();
        c.type = ConversationType.DIRECT;
        c.participantIds = new ArrayList<>(List.of(userA, userB));
        return c;
    }
    public static Conversation createDivisionGroup(Long divisionId, String title) {
        Conversation c = new Conversation();
        c.type = ConversationType.DIVISION;
        c.divisionId = divisionId;
        c.title = title;
        c.participantIds = new ArrayList<>();
        return c;
    }
    public Long getId() {
        return id;
    }
    public List<Long> getParticipantIds() {
        return participantIds;
    }
    public String getTitle() {
        return title;
    }
    public ConversationType getType() {
        return type;
    }
    public Academy getAcademy() {
        return academy;
    }
    public Long getDivisionId() {
        return divisionId;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setParticipantIds(List<Long> participantIds) {
        this.participantIds = participantIds;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setAcademy(Academy academy) {
        this.academy = academy;
    }
    public void setDivisionId(Long divisionId) {
        this.divisionId = divisionId;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    public void setType(ConversationType type) {
        this.type = type;
    }
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
