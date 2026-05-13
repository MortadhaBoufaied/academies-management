package com.footballacademy.DTO;

import jakarta.validation.constraints.*;

/**  * Data Transfer Object for Player with comprehensive validation  */
public
class PlayerDTO {
    private Long id;
    @NotBlank(message = "Player name is required")
    @Size(min = 2, max = 100, message = "Player name must be between 2 and 100 characters")
    private String nom;
    @Size(max = 50, message = "Position cannot exceed 50 characters")
    @Pattern(regexp = "^[A-Z]{2,4}$", message = "Position must be 2-4 uppercase letters")
    private String position;
    @Min(value = 5, message = "Age must be at least 5")
    @Max(value = 50, message = "Age must not exceed 50")
    private int age;
    private boolean paid;
    @Size(max = 500, message = "Image URL cannot exceed 500 characters")
    @Pattern(regexp = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$", message = "Image URL must be a valid URL")
    private String imageUrl;
    // Additional fields for comprehensive player data
    @Size(max = 100, message = "Nationality cannot exceed 100 characters")
    private String nationality;
    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    @Pattern(regexp = "^[+]?[0-9\\-\\s]+$", message = "Phone number can only contain digits, spaces, hyphens, and plus sign")
    private String phone;
    @Min(value = 0, message = "Height cannot be negative")
    @Max(value = 300, message = "Height cannot exceed 300 cm")
    private Double height;
    @Min(value = 0, message = "Weight cannot be negative")
    @Max(value = 300, message = "Weight cannot exceed 300 kg")
    private Double weight;
    @Min(value = 0, message = "Goals cannot be negative")
    private Integer goals;
    @Min(value = 0, message = "Assists cannot be negative")
    private Integer assists;
    @Min(value = 0, message = "Matches cannot be negative")
    private Integer matches;
    @DecimalMin(value = "0.0", message = "Average rating cannot be negative")
    @DecimalMax(value = "10.0", message = "Average rating cannot exceed 10.0")
    private Double averageRating;
    private Long divisionId;
    private Long parentId;
    private Long trainerId;
    // Enrichment fields
    private String divisionName;
    private String parentName;
    private String trainerName;
    public PlayerDTO() {
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
    public String getPosition() {
        return position;
    }
    public void setPosition(String position) {
        this.position = position;
    }
    public int getAge() {
        return age;
    }
    public void setAge(int age) {
        this.age = age;
    }
    public boolean isPaid() {
        return paid;
    }
    public void setPaid(boolean paid) {
        this.paid = paid;
    }
    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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
    public Long getDivisionId() {
        return divisionId;
    }
    public void setDivisionId(Long divisionId) {
        this.divisionId = divisionId;
    }
    public Long getParentId() {
        return parentId;
    }
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
    public Long getTrainerId() {
        return trainerId;
    }
    public void setTrainerId(Long trainerId) {
        this.trainerId = trainerId;
    }
    public String getDivisionName() {
        return divisionName;
    }
    public void setDivisionName(String divisionName) {
        this.divisionName = divisionName;
    }
    public String getParentName() {
        return parentName;
    }
    public void setParentName(String parentName) {
        this.parentName = parentName;
    }
    public String getTrainerName() {
        return trainerName;
    }
    public void setTrainerName(String trainerName) {
        this.trainerName = trainerName;
    }
    // Business logic methods
    public double getGoalsPerMatch() {
        return matches > 0 ?(double) goals / matches : 0.0;
    }
    public double getAssistsPerMatch() {
        return matches > 0 ?(double) assists / matches : 0.0;
    }
    public double getTotalContributions() {
        return(goals != null ? goals : 0) +(assists != null ? assists : 0);
    }
}
