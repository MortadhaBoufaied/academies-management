package com.footballacademy.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList; import java.util.List;
@Entity @Table(name="scouting_reports", indexes={@Index(name="idx_report_player", columnList="player_id"),@Index(name="idx_report_scouter", columnList="scouter_id"),@Index(name="idx_report_academy", columnList="academy_id"),@Index(name="idx_report_match", columnList="match_id"),@Index(name="idx_report_status", columnList="status")})
public class ScoutingReport {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="player_id") @JsonIgnore private Player player;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="scouter_id") @JsonIgnore private Scouter scouter;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="academy_id") @JsonIgnore private Academy academy;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="match_id") @JsonIgnore private Match match;
 private Double technicalScore=0.0,tacticalScore=0.0,physicalScore=0.0,mentalScore=0.0,potentialScore=0.0,styleFitScore=0.0;
 @Column(length=500) private String recommendation; @Column(length=4000) private String notes;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private ScoutingStatus status=ScoutingStatus.DRAFT;
 private LocalDateTime createdAt=LocalDateTime.now(); private LocalDateTime updatedAt=LocalDateTime.now();
 @OneToMany(mappedBy="report", cascade=CascadeType.ALL, orphanRemoval=true) private List<ScoutingCriterionScore> criterionScores=new ArrayList<>();
 @PreUpdate public void preUpdate(){updatedAt=LocalDateTime.now();}
 public double calculateOverallScore(){double[] v={n(technicalScore),n(tacticalScore),n(physicalScore),n(mentalScore),n(potentialScore),n(styleFitScore)}; double s=0; for(double x:v)s+=x; return Math.round((s/v.length)*100.0)/100.0;}
 private double n(Double d){return d==null?0.0:d;} public void approve(){status=ScoutingStatus.APPROVED; updatedAt=LocalDateTime.now();} public void reject(){status=ScoutingStatus.REJECTED; updatedAt=LocalDateTime.now();}
 public Long getId(){return id;} public void setId(Long id){this.id=id;} public Player getPlayer(){return player;} public void setPlayer(Player player){this.player=player;} public Scouter getScouter(){return scouter;} public void setScouter(Scouter scouter){this.scouter=scouter;} public Academy getAcademy(){return academy;} public void setAcademy(Academy academy){this.academy=academy;} public Match getMatch(){return match;} public void setMatch(Match match){this.match=match;} public Double getTechnicalScore(){return technicalScore;} public void setTechnicalScore(Double v){technicalScore=v;} public Double getTacticalScore(){return tacticalScore;} public void setTacticalScore(Double v){tacticalScore=v;} public Double getPhysicalScore(){return physicalScore;} public void setPhysicalScore(Double v){physicalScore=v;} public Double getMentalScore(){return mentalScore;} public void setMentalScore(Double v){mentalScore=v;} public Double getPotentialScore(){return potentialScore;} public void setPotentialScore(Double v){potentialScore=v;} public Double getStyleFitScore(){return styleFitScore;} public void setStyleFitScore(Double v){styleFitScore=v;} public String getRecommendation(){return recommendation;} public void setRecommendation(String recommendation){this.recommendation=recommendation;} public String getNotes(){return notes;} public void setNotes(String notes){this.notes=notes;} public ScoutingStatus getStatus(){return status;} public void setStatus(ScoutingStatus status){this.status=status;} public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime createdAt){this.createdAt=createdAt;} public LocalDateTime getUpdatedAt(){return updatedAt;} public void setUpdatedAt(LocalDateTime updatedAt){this.updatedAt=updatedAt;} public List<ScoutingCriterionScore> getCriterionScores(){return criterionScores;} public void setCriterionScores(List<ScoutingCriterionScore> criterionScores){this.criterionScores=criterionScores;}
}
