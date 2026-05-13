package com.footballacademy.DTO;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**  * Data Transfer Object for Payment with comprehensive validation  */
public
class PaymentDto {
    private Long id;
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    private Double montant;
    @NotNull(message = "Month is required")
    @PastOrPresent(message = "Month cannot be in the future")
    private LocalDate mois;
    private boolean isPaid;
    @NotNull(message = "Player ID is required")
    private Long playerId;
    @NotNull(message = "Parent ID is required")
    private Long parentId;
    // Additional fields for payment gateway
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter ISO code")
    private String currency;
    @Size(max = 50, message = "Payment type cannot exceed 50 characters")
    private String paymentType;
    @PastOrPresent(message = "Due date cannot be in the future")
    private LocalDate dueDate;
    @Size(max = 20, message = "Status cannot exceed 20 characters")
    @Pattern(regexp = "^(PENDING|PAID|FAILED|CANCELLED)$", message = "Status must be one of: PENDING, PAID, FAILED, CANCELLED")
    private String status;
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    // Fields for enrichment (not validated)
    private String playerName;
    private String parentName;
    private String parentEmail;
    // Constructors
    public PaymentDto() {
    }
    public PaymentDto(Long id, Double montant, LocalDate mois, boolean isPaid, Long playerId, Long parentId) {
        this.id = id;
        this.montant = montant;
        this.mois = mois;
        this.isPaid = isPaid;
        this.playerId = playerId;
        this.parentId = parentId;
    }
    // Getters and Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Double getMontant() {
        return montant;
    }
    public void setMontant(Double montant) {
        this.montant = montant;
    }
    public LocalDate getMois() {
        return mois;
    }
    public void setMois(LocalDate mois) {
        this.mois = mois;
    }
    public boolean isPaid() {
        return isPaid;
    }
    public void setPaid(boolean paid) {
        this.isPaid = paid;
    }
    public Long getPlayerId() {
        return playerId;
    }
    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }
    public Long getParentId() {
        return parentId;
    }
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
    public String getCurrency() {
        return currency;
    }
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    public String getPaymentType() {
        return paymentType;
    }
    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }
    public LocalDate getDueDate() {
        return dueDate;
    }
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getPlayerName() {
        return playerName;
    }
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
    public String getParentName() {
        return parentName;
    }
    public void setParentName(String parentName) {
        this.parentName = parentName;
    }
    public String getParentEmail() {
        return parentEmail;
    }
    public void setParentEmail(String parentEmail) {
        this.parentEmail = parentEmail;
    }
    // Business logic methods
    public boolean isOverdue() {
        return !isPaid && dueDate != null && dueDate.isBefore(LocalDate.now());
    }
    public long getDaysOverdue() {
        if (!isOverdue()) {
            return 0;
        } return java.time.temporal.ChronoUnit.DAYS.between(dueDate, LocalDate.now());
    }
}
