package com.footballacademy.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "players", indexes = {
    @Index(name = "idx_player_division", columnList = "division_id"),
    @Index(name = "idx_player_parent", columnList = "parent_id"),
    @Index(name = "idx_player_trainer", columnList = "trainer_id"),
    @Index(name = "idx_player_academy", columnList = "academy_id"),
    @Index(name = "idx_player_sport", columnList = "sport_id"),
    @Index(name = "idx_player_sport_position", columnList = "sport_position_id"),
    @Index(name = "idx_player_paid", columnList = "is_paid"),
    @Index(name = "idx_player_position", columnList = "position"),
    @Index(name = "idx_player_age", columnList = "age"),
    @Index(name = "idx_player_goals", columnList = "goals"),
    @Index(name = "idx_player_matches", columnList = "matches")
})
public
class Player {
    /**      * Shared primary key with User.      * IMPORTANT: in DB the PK column is user_id (no separate id column).      */
    @Id
    @Column(name = "user_id")
    private Long id;
    // If you store a User row for each player, keep the relation (no cascade)
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "user_id", unique = true)
    @JsonIgnore
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academy_id", foreignKey =
    @ForeignKey(name = "fk_player_academy"))
    @JsonIgnore
    private Academy academy;
    private String position;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sport_id", foreignKey =
    @ForeignKey(name = "fk_player_sport"))
    @JsonIgnore
    private Sport sport;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sport_position_id", foreignKey =
    @ForeignKey(name = "fk_player_sport_position"))
    @JsonIgnore
    private SportPosition sportPosition;
    @Column(name = "custom_stats", columnDefinition = "TEXT")
    private String customStats;
    private Integer age;
    private String nationality;
    private String phone;
    @Column(name = "image_url")
    private String imageUrl;
    @Column(name = "is_paid")
    private boolean isPaid;
    private Double height;
    private Double weight;
    private Integer goals;
    private Integer assists;
    private Integer matches;
    @Column(name = "average_rating")
    private Double averageRating;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "division_id", foreignKey =
    @ForeignKey(name = "fk_player_division"))
    @JsonIgnore
    private Division division;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", foreignKey =
    @ForeignKey(name = "fk_player_parent"))
    @JsonIgnore
    private Parent parent;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", foreignKey =
    @ForeignKey(name = "fk_player_trainer"))
    @JsonIgnore
    private Trainer trainer;
    public Player() {
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public Academy getAcademy() {
        return academy;
    }
    public void setAcademy(Academy academy) {
        this.academy = academy;
    }
    public String getPosition() {
        return position;
    }
    public void setPosition(String position) {
        this.position = position;
    }
    public Sport getSport() {
        return sport;
    }
    public void setSport(Sport sport) {
        this.sport = sport;
    }
    public SportPosition getSportPosition() {
        return sportPosition;
    }
    public void setSportPosition(SportPosition sportPosition) {
        this.sportPosition = sportPosition;
    }
    public String getCustomStats() {
        return customStats;
    }
    public void setCustomStats(String customStats) {
        this.customStats = customStats;
    }
    public Integer getAge() {
        return age;
    }
    public void setAge(Integer age) {
        this.age = age;
    }
    public String getNationality() {
        return nationality;
    }
    public void setNationality(String nationality) {
        this.nationality = nationality;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    public boolean isPaid() {
        return isPaid;
    }
    public void setPaid(boolean paid) {
        isPaid = paid;
    }
    public Double getHeight() {
        return height;
    }
    public void setHeight(Double height) {
        this.height = height;
    }
    public Double getWeight() {
        return weight;
    }
    public void setWeight(Double weight) {
        this.weight = weight;
    }
    public Integer getGoals() {
        return goals;
    }
    public void setGoals(Integer goals) {
        this.goals = goals;
    }
    public Integer getAssists() {
        return assists;
    }
    public void setAssists(Integer assists) {
        this.assists = assists;
    }
    public Integer getMatches() {
        return matches;
    }
    public void setMatches(Integer matches) {
        this.matches = matches;
    }
    public Double getAverageRating() {
        return averageRating;
    }
    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }
    public Division getDivision() {
        return division;
    }
    public void setDivision(Division division) {
        this.division = division;
    }
    public Parent getParent() {
        return parent;
    }
    public void setParent(Parent parent) {
        this.parent = parent;
    }
    public Trainer getTrainer() {
        return trainer;
    }
    public void setTrainer(Trainer trainer) {
        this.trainer = trainer;
    }
}
