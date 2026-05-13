package com.footballacademy.DTO;

public
class DivisionSummaryDTO {
    private Long id;
    private String nom;
    private String categorie;
    private int playerCount;
    private Double averageAge;
    public DivisionSummaryDTO() {
    }
    public DivisionSummaryDTO(Long id, String nom, String categorie, int playerCount, Double averageAge) {
        this.id = id;
        this.nom = nom;
        this.categorie = categorie;
        this.playerCount = playerCount;
        this.averageAge = averageAge;
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
}
