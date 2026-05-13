package com.footballacademy.DTO;

import java.util.List;

public
class DivisionDetailDTO {
    private Long id;
    private String nom;
    private String categorie;
    private int playerCount;
    private Double averageAge;
    private List<PlayerDTO> players;
    public DivisionDetailDTO() {
    }
    // Getters & Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getNom() {
        return nom;
    }
    public void setNom(String nom) {
        this.nom = nom;
    }
    public String getCategorie() {
        return categorie;
    }
    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }
    public int getPlayerCount() {
        return playerCount;
    }
    public void setPlayerCount(int playerCount) {
        this.playerCount = playerCount;
    }
    public Double getAverageAge() {
        return averageAge;
    }
    public void setAverageAge(Double averageAge) {
        this.averageAge = averageAge;
    }
    public List<PlayerDTO> getPlayers() {
        return players;
    }
    public void setPlayers(List<PlayerDTO> players) {
        this.players = players;
    }
}
