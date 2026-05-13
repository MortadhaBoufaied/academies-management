package com.footballacademy.model;

import jakarta.persistence.*;
import jakarta.persistence.Transient;
import java.time.LocalDate;

@Entity
@Table(name = "activities", indexes = {
    @Index(name = "idx_activity_trainer_id", columnList = "trainer_id"),
    @Index(name = "idx_activity_academy", columnList = "academy_id"),
    @Index(name = "idx_activity_date", columnList = "date"),
    @Index(name = "idx_activity_titre", columnList = "titre"),
    @Index(name = "idx_activity_lieu", columnList = "lieu")
})
@Inheritance(strategy = InheritanceType.JOINED)
public
class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "trainer_id")
    private Long trainerId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academy_id", foreignKey =
    @ForeignKey(name = "fk_activity_academy"))
    private Academy academy;
    @Column(nullable = false)
    private String titre;
    private String description;
    @Column(nullable = false)
    private LocalDate date;
    private String lieu;
    // Constructeurs
    public Activity() {
    }
    public Activity(Long trainerId, String titre, String description, LocalDate date, String lieu) {
        this.trainerId = trainerId;
        this.titre = titre;
        this.description = description;
        this.date = date;
        this.lieu = lieu;
    }
    // Getters et Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getTrainerId() {
        return trainerId;
    }
    public void setTrainerId(Long trainerId) {
        this.trainerId = trainerId;
    }
    public Academy getAcademy() {
        return academy;
    }
    public void setAcademy(Academy academy) {
        this.academy = academy;
    }
    public String getTitre() {
        return titre;
    }
    public void setTitre(String titre) {
        this.titre = titre;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public LocalDate getDate() {
        return date;
    }
    public void setDate(LocalDate date) {
        this.date = date;
    }
    public String getLieu() {
        return lieu;
    }
    public void setLieu(String lieu) {
        this.lieu = lieu;
    }
    @Transient
    public String getType() {
        if (this instanceof Match) return "MATCH";
        if (this instanceof Training) return "TRAINING";
        return "ACTIVITY";
    }
    @Transient
    public String getLocation() {
        return this.getLieu();
    }
}
