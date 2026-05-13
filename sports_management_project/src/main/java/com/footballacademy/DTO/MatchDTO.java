package com.footballacademy.DTO;

import com.footballacademy.model.Match;
import java.time.LocalDate;

/**  * Lightweight DTO for mobile clients.  * Avoids exposing JPA entities directly (lazy proxies / large nested graphs).  */
public
class MatchDTO {
    private Long id;
    private String type;
    private LocalDate date;
    private String location;
    private Long trainerId;
    private Long academyId;
    private String opponent;
    private String result;
    public static MatchDTO from(Match m) {
        MatchDTO dto = new MatchDTO();
        if (m == null) return dto;
        dto.id = m.getId();
        dto.type = "MATCH";
        dto.date = m.getDate();
        dto.location = m.getLieu();
        dto.trainerId = m.getTrainerId();
        dto.academyId = m.getAcademy() != null ? m.getAcademy() .getId() : null;
        dto.opponent = m.getOpponent();
        dto.result = m.getResult();
        return dto;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public LocalDate getDate() {
        return date;
    }
    public void setDate(LocalDate date) {
        this.date = date;
    }
    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }
    public Long getTrainerId() {
        return trainerId;
    }
    public void setTrainerId(Long trainerId) {
        this.trainerId = trainerId;
    }
    public Long getAcademyId() {
        return academyId;
    }
    public void setAcademyId(Long academyId) {
        this.academyId = academyId;
    }
    public String getOpponent() {
        return opponent;
    }
    public void setOpponent(String opponent) {
        this.opponent = opponent;
    }
    public String getResult() {
        return result;
    }
    public void setResult(String result) {
        this.result = result;
    }
}
