package com.footballacademy.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "player_rankings", uniqueConstraints =
@UniqueConstraint(name = "uk_rank_player", columnNames = {
    "player_id"
}))
public
class PlayerRanking {
    public
    enum Tier {
        ELITE, CORE, DEVELOPING
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "player_id", nullable = false)
    private Long playerId;
    // Optional view-only relation (no extra FK writes)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", insertable = false, updatable = false)
    private Player player;
    @Column(nullable = false)
    private double score;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tier tier;
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;
    public double getScore() {
        return score;
    }
    public void setScore(double score) {
        this.score = score;
    }
    public Tier getTier() {
        return tier;
    }
    public void setTier(Tier tier) {
        this.tier = tier;
    }
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    public Player getPlayer() {
        return player;
    }
    public void setPlayer(Player player) {
        this.player = player;
    }
    public Long getPlayerId() {
        return playerId;
    }
    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
}
