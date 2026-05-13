package com.footballacademy.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "academy_performance_scores", indexes = {
    @Index(name = "idx_academy_score_academy", columnList = "academy_id"),
    @Index(name = "idx_academy_score_sport", columnList = "sport_id"),
    @Index(name = "idx_academy_score_overall", columnList = "overall_score"),
    @Index(name = "idx_academy_score_rank", columnList = "ranking_position"),
    @Index(name = "idx_academy_score_generated", columnList = "generated_at")
})
public class AcademyPerformanceScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "academy_id", foreignKey = @ForeignKey(name = "fk_score_academy"))
    @JsonIgnore
    private Academy academy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sport_id", foreignKey = @ForeignKey(name = "fk_score_sport"))
    @JsonIgnore
    private Sport sport;

    @Column(name = "overall_score")
    private Double overallScore;
    @Column(name = "player_development_score")
    private Double playerDevelopmentScore;
    @Column(name = "scouting_score")
    private Double scoutingScore;
    @Column(name = "activity_score")
    private Double activityScore;
    @Column(name = "payment_health_score")
    private Double paymentHealthScore;
    @Column(name = "talent_production_score")
    private Double talentProductionScore;
    @Column(name = "ranking_position")
    private Integer rankingPosition;
    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt = LocalDateTime.now();
    @Column(length = 2000)
    private String explanation;
    @Column(name = "main_strengths", length = 2000)
    private String mainStrengths;
    @Column(name = "main_weaknesses", length = 2000)
    private String mainWeaknesses;
    private Double confidence;

    @PrePersist
    protected void onCreate() {
        if (generatedAt == null) generatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Academy getAcademy() { return academy; }
    public void setAcademy(Academy academy) { this.academy = academy; }
    public Sport getSport() { return sport; }
    public void setSport(Sport sport) { this.sport = sport; }
    public Long getAcademyId() { return academy != null ? academy.getId() : null; }
    public Long getSportId() { return sport != null ? sport.getId() : null; }
    public Double getOverallScore() { return overallScore; }
    public void setOverallScore(Double overallScore) { this.overallScore = overallScore; }
    public Double getPlayerDevelopmentScore() { return playerDevelopmentScore; }
    public void setPlayerDevelopmentScore(Double playerDevelopmentScore) { this.playerDevelopmentScore = playerDevelopmentScore; }
    public Double getScoutingScore() { return scoutingScore; }
    public void setScoutingScore(Double scoutingScore) { this.scoutingScore = scoutingScore; }
    public Double getActivityScore() { return activityScore; }
    public void setActivityScore(Double activityScore) { this.activityScore = activityScore; }
    public Double getPaymentHealthScore() { return paymentHealthScore; }
    public void setPaymentHealthScore(Double paymentHealthScore) { this.paymentHealthScore = paymentHealthScore; }
    public Double getTalentProductionScore() { return talentProductionScore; }
    public void setTalentProductionScore(Double talentProductionScore) { this.talentProductionScore = talentProductionScore; }
    public Integer getRankingPosition() { return rankingPosition; }
    public void setRankingPosition(Integer rankingPosition) { this.rankingPosition = rankingPosition; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public String getMainStrengths() { return mainStrengths; }
    public void setMainStrengths(String mainStrengths) { this.mainStrengths = mainStrengths; }
    public String getMainWeaknesses() { return mainWeaknesses; }
    public void setMainWeaknesses(String mainWeaknesses) { this.mainWeaknesses = mainWeaknesses; }
    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
}
