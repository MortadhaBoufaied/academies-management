package com.footballacademy.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "matches", indexes = {
    @Index(name = "idx_match_opponent", columnList = "opponent"),
    @Index(name = "idx_match_result", columnList = "result")
})
@PrimaryKeyJoinColumn(name = "activity_id")
public
class Match extends Activity {
    private String opponent;
    private String result;
    // Constructeurs
    public Match() {
    }
    public Match(Long trainerId, String titre, String description, LocalDate date, String lieu, String opponent) {
        super(trainerId, titre, description, date, lieu);
        this.opponent = opponent;
    }
    // Getters et Setters
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
