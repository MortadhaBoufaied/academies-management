package com.footballacademy.DTO;

import jakarta.validation.constraints.*;

/**  * Data Transfer Object for Division with comprehensive validation  */
public
class DivisionDto {
    private Long id;
    @NotBlank(message = "Division name is required")
    @Size(min = 2, max = 100, message = "Division name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s\\-]+$", message = "Division name can only contain letters, numbers, spaces, and hyphens")
    private String nom;
    @Size(max = 50, message = "Category cannot exceed 50 characters")
    private String categorie;
    @NotNull(message = "Sport ID is required")
    private Long sportId;
    private String sportName;
    private String sportCode;
    // Constructors
    public DivisionDto() {
    }
    public DivisionDto(String nom, String categorie) {
        this.nom = nom;
        this.categorie = categorie;
    }
    // Getters and Setters
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
    public Long getSportId() {
        return sportId;
    }
    public void setSportId(Long sportId) {
        this.sportId = sportId;
    }
    public String getSportName() {
        return sportName;
    }
    public void setSportName(String sportName) {
        this.sportName = sportName;
    }
    public String getSportCode() {
        return sportCode;
    }
    public void setSportCode(String sportCode) {
        this.sportCode = sportCode;
    }
}
