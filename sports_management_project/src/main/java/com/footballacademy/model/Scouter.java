package com.footballacademy.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "scouters", indexes = {
        @Index(name = "idx_scouter_academy", columnList = "academy_id"),
        @Index(name = "idx_scouter_region", columnList = "region"),
        @Index(name = "idx_scouter_active", columnList = "active")
})
public class Scouter {
    @Id
    @Column(name = "user_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academy_id")
    @JsonIgnore
    private Academy academy;

    private String region;
    private String speciality;
    private String experienceLevel;
    private Boolean active = true;

    @OneToMany(mappedBy = "scouter", cascade = CascadeType.ALL, orphanRemoval = false)
    @JsonIgnore
    private List<ScoutingAssignment> assignments = new ArrayList<>();

    @OneToMany(mappedBy = "scouter", cascade = CascadeType.ALL, orphanRemoval = false)
    @JsonIgnore
    private List<ScoutingReport> reports = new ArrayList<>();

    public Scouter() {}
    public Scouter(User user) { this.user = user; if (user != null) { this.id = user.getId(); this.academy = user.getAcademy(); } }

    public ScoutingReport evaluatePlayer(Player player) { ScoutingReport report = new ScoutingReport(); report.setPlayer(player); report.setScouter(this); report.setAcademy(this.academy); return report; }
    public void shortlistPlayer(Player player) { }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Academy getAcademy() { return academy; }
    public void setAcademy(Academy academy) { this.academy = academy; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getSpeciality() { return speciality; }
    public void setSpeciality(String speciality) { this.speciality = speciality; }
    public String getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(String experienceLevel) { this.experienceLevel = experienceLevel; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public boolean isActive() { return Boolean.TRUE.equals(active); }
    public List<ScoutingAssignment> getAssignments() { return assignments; }
    public void setAssignments(List<ScoutingAssignment> assignments) { this.assignments = assignments; }
    public List<ScoutingReport> getReports() { return reports; }
    public void setReports(List<ScoutingReport> reports) { this.reports = reports; }
}
