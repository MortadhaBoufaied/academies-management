package com.footballacademy.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "trainers", indexes = {
    @Index(name = "idx_trainer_academy", columnList = "academy_id")
})
public
class Trainer {
    /** Shared PK with User stored in column user_id */
    @Id
    @Column(name = "user_id")
    private Long id;
    private String speciality;
    private String experience;
    private String license;
    private String notes;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academy_id", foreignKey = @ForeignKey(name = "fk_trainer_academy"))
    @JsonIgnore
    private Academy academy;
    // Trainer owns the relationship via trainers.division_id (see Division.java comment)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "division_id", foreignKey = @ForeignKey(name = "fk_trainer_division"))
    @JsonIgnore
    private Division division;
    // NEW: A trainer can coach multiple divisions
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "trainer_divisions", joinColumns =
    @JoinColumn(name = "trainer_id"), inverseJoinColumns =
    @JoinColumn(name = "division_id"))
    @JsonIgnore
    private Set<Division> divisions = new HashSet<>();
    public Trainer() {
    }
    public Trainer(User user) {
        this.user = user;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getSpeciality() {
        return speciality;
    }
    public void setSpeciality(String speciality) {
        this.speciality = speciality;
    }
    public String getExperience() {
        return experience;
    }
    public void setExperience(String experience) {
        this.experience = experience;
    }
    public String getLicense() {
        return license;
    }
    public void setLicense(String license) {
        this.license = license;
    }
    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
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
    public Division getDivision() {
        return division;
    }
    public void setDivision(Division division) {
        this.division = division;
    }
    public Set<Division> getDivisions() {
        return divisions;
    }
    public void setDivisions(Set<Division> divisions) {
        this.divisions = divisions;
    }
}
