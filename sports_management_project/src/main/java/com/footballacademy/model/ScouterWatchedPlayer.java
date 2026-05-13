package com.footballacademy.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "scouter_watched_players", indexes = {
    @Index(name = "idx_watch_scouter", columnList = "scouter_id"),
    @Index(name = "idx_watch_player", columnList = "player_id"),
    @Index(name = "idx_watch_academy", columnList = "academy_id"),
    @Index(name = "idx_watch_sport", columnList = "sport_id"),
    @Index(name = "idx_watch_division", columnList = "division_id"),
    @Index(name = "idx_watch_status", columnList = "watch_status"),
    @Index(name = "idx_watch_priority", columnList = "priority")
})
public class ScouterWatchedPlayer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scouter_id", foreignKey = @ForeignKey(name = "fk_watch_scouter"))
    @JsonIgnore
    private Scouter scouter;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", foreignKey = @ForeignKey(name = "fk_watch_player"))
    @JsonIgnore
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academy_id", foreignKey = @ForeignKey(name = "fk_watch_academy"))
    @JsonIgnore
    private Academy academy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sport_id", foreignKey = @ForeignKey(name = "fk_watch_sport"))
    @JsonIgnore
    private Sport sport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "division_id", foreignKey = @ForeignKey(name = "fk_watch_division"))
    @JsonIgnore
    private Division division;

    @Column(name = "watch_status", nullable = false, length = 40)
    private String watchStatus = "WATCHING";

    @Column(nullable = false, length = 40)
    private String priority = "MEDIUM";

    @Column(length = 2000)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "last_reviewed_at")
    private LocalDateTime lastReviewedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (watchStatus == null || watchStatus.isBlank()) watchStatus = "WATCHING";
        if (priority == null || priority.isBlank()) priority = "MEDIUM";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (watchStatus == null || watchStatus.isBlank()) watchStatus = "WATCHING";
        if (priority == null || priority.isBlank()) priority = "MEDIUM";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Scouter getScouter() { return scouter; }
    public void setScouter(Scouter scouter) { this.scouter = scouter; }
    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }
    public Academy getAcademy() { return academy; }
    public void setAcademy(Academy academy) { this.academy = academy; }
    public Sport getSport() { return sport; }
    public void setSport(Sport sport) { this.sport = sport; }
    public Division getDivision() { return division; }
    public void setDivision(Division division) { this.division = division; }
    public String getWatchStatus() { return watchStatus; }
    public void setWatchStatus(String watchStatus) { this.watchStatus = normalize(watchStatus, "WATCHING"); }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = normalize(priority, "MEDIUM"); }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getLastReviewedAt() { return lastReviewedAt; }
    public void setLastReviewedAt(LocalDateTime lastReviewedAt) { this.lastReviewedAt = lastReviewedAt; }
    public Long getScouterId() { return scouter != null ? scouter.getId() : null; }
    public Long getPlayerId() { return player != null ? player.getId() : null; }
    public Long getAcademyId() { return academy != null ? academy.getId() : null; }
    public Long getSportId() { return sport != null ? sport.getId() : null; }
    public Long getDivisionId() { return division != null ? division.getId() : null; }

    private String normalize(String value, String fallback) {
        if (value == null || value.isBlank()) return fallback;
        return value.trim().toUpperCase();
    }
}
