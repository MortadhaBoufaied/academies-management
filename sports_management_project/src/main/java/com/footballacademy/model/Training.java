package com.footballacademy.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trainings")
@PrimaryKeyJoinColumn(name = "activity_id")
public
class Training extends Activity {
    private String sessionType;
    private String objectives;
    
    @ElementCollection
    @CollectionTable(name = "training_attendees", joinColumns = @JoinColumn(name = "training_id"))
    @Column(name = "attendee_id")
    private List<Long> attendeeIds = new ArrayList<>();
    
    // Constructors
    public Training() {
    }
    public Training(Long trainerId, String titre, String description, LocalDate date, String lieu, String sessionType, String objectives) {
        super(trainerId, titre, description, date, lieu);
        this.sessionType = sessionType;
        this.objectives = objectives;
    }
    // Getters and Setters
    public String getSessionType() {
        return sessionType;
    }
    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }
    public String getObjectives() {
        return objectives;
    }
    public void setObjectives(String objectives) {
        this.objectives = objectives;
    }
    
    public List<Long> getAttendeeIds() {
        return attendeeIds;
    }
    
    public void setAttendeeIds(List<Long> attendeeIds) {
        this.attendeeIds = attendeeIds;
    }
}
