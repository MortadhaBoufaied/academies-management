package com.footballacademy.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments", uniqueConstraints = {
    @UniqueConstraint(name = "uk_payment_player_month", columnNames = {
        "player_id", "mois"
    })
}, indexes = {
    @Index(name = "idx_payment_status", columnList = "status"),
    @Index(name = "idx_payment_due_date", columnList = "due_date"),
    @Index(name = "idx_payment_academy", columnList = "academy_id"),
    @Index(name = "idx_payment_player", columnList = "player_id"),
    @Index(name = "idx_payment_parent", columnList = "parent_id")
})
public
class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // Optimistic locking version field for concurrent update protection
    @Version
    private Long version;
    // Existing French field names (maintained for backward compatibility)
    private Double montant;
    @Column(nullable = false)
    private LocalDate mois;
    @Column(name = "is_paid")
    private boolean isPaid;
    // New proper relationships instead of plain Long fields
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", foreignKey =
    @ForeignKey(name = "fk_payment_player"))
    @JsonIgnore
    private Player player;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", foreignKey =
    @ForeignKey(name = "fk_payment_parent"))
    @JsonIgnore
    private Parent parent;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academy_id", foreignKey =
    @ForeignKey(name = "fk_payment_academy"))
    @JsonIgnore
    private Academy academy;
    // New fields for payment gateway integration
    @Column(name = "currency", length = 3)
    private String currency = "USD";
    @Column(name = "payment_type", length = 50)
    private String paymentType = "MONTHLY_FEE";
    @Column(name = "due_date")
    private LocalDate dueDate;
    @Column(name = "status", length = 20)
    private String status = "PENDING";
    @Column(name = "description", length = 500)
    private String description;
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    @Column(name = "failed_reason", length = 500)
    private String failedReason;
    // Constructors
    public Payment() {
        this.currency = "USD";
        this.paymentType = "MONTHLY_FEE";
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    public Payment(Double montant, LocalDate mois, Long playerId, Long parentId) {
        this();
        this.montant = montant;
        this.mois = mois;
        this.playerId = playerId;
        this.parentId = parentId;
        this.isPaid = false;
    }
    public Payment(Double montant, LocalDate mois, Player player, Parent parent) {
        this();
        this.montant = montant;
        this.mois = mois;
        this.player = player;
        this.parent = parent;
        this.isPaid = false;
        if (player != null) {
            this.playerId = player.getId();
        }
        if (parent != null) {
            this.parentId = parent.getId();
        }
    }
    // Legacy fields for backward compatibility (marked as deprecated)
    @Deprecated
    @Column(name = "player_id", insertable = false, updatable = false)
    private Long playerId;
    @Deprecated
    @Column(name = "parent_id", insertable = false, updatable = false)
    private Long parentId;
    // Getters and Setters - Existing French field names
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getVersion() {
        return version;
    }
    public void setVersion(Long version) {
        this.version = version;
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
        if (paid) {
            this.status = "PAID";
            this.completedAt = LocalDateTime.now();
        } else {
            this.status = "PENDING";
            this.completedAt = null;
        }
    }
    // Relationship getters and setters
    public Player getPlayer() {
        return player;
    }
    public void setPlayer(Player player) {
        this.player = player;
        if (player != null) {
            this.playerId = player.getId();
        }
    }
    public Parent getParent() {
        return parent;
    }
    public void setParent(Parent parent) {
        this.parent = parent;
        if (parent != null) {
            this.parentId = parent.getId();
        }
    }
    public Academy getAcademy() {
        return academy;
    }
    public void setAcademy(Academy academy) {
        this.academy = academy;
    }
    // Legacy getters for backward compatibility (to be removed in future version)
    @Deprecated
    public Long getPlayerId() {
        return player != null ? player.getId() : playerId;
    }
    @Deprecated
    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }
    @Deprecated
    public Long getParentId() {
        return parent != null ? parent.getId() : parentId;
    }
    @Deprecated
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
    // New getters and setters for payment gateway integration
    public Double getAmount() {
        return this.montant;
    }
    public void setAmount(Double amount) {
        this.montant = amount;
    }
    public String getCurrency() {
        return this.currency;
    }
    public void setCurrency(String currency) {
        if (currency != null && currency.length() == 3) {
            this.currency = currency.toUpperCase();
        }
    }
    public String getPaymentType() {
        return this.paymentType;
    }
    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType != null ? paymentType.toUpperCase() : "MONTHLY_FEE";
    }
    public String getType() {
        return this.paymentType;
    }
    public void setType(String type) {
        this.setPaymentType(type);
    }
    public LocalDate getDueDate() {
        return this.dueDate;
    }
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
    public String getStatus() {
        return this.status;
    }
    public void setStatus(String status) {
        if (status != null) {
            this.status = status.toUpperCase();
            this.isPaid = "PAID" .equals(this.status);
            if (this.isPaid && this.completedAt == null) {
                this.completedAt = LocalDateTime.now();
            }
        }
    }
    public String getDescription() {
        return this.description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    public String getFailedReason() {
        return failedReason;
    }
    public void setFailedReason(String failedReason) {
        this.failedReason = failedReason;
    }
    // Business logic methods
    public boolean isOverdue() {
        return !this.isPaid && this.dueDate != null && this.dueDate.isBefore(LocalDate.now());
    }
    public long getDaysOverdue() {
        if (!isOverdue()) {
            return 0;
        } return java.time.temporal.ChronoUnit.DAYS.between(this.dueDate, LocalDate.now());
    }
    public boolean isPending() {
        return "PENDING" .equals(this.status) && !this.isPaid;
    }
    public boolean isFailed() {
        return "FAILED" .equals(this.status);
    }
    public boolean isCancelled() {
        return "CANCELLED" .equals(this.status);
    }
    public void markAsPaid() {
        this.setPaid(true);
        this.setStatus("PAID");
        this.setFailedReason(null);
    }
    public void markAsFailed(String reason) {
        this.setStatus("FAILED");
        this.setFailedReason(reason);
    }
    public void markAsCancelled(String reason) {
        this.setStatus("CANCELLED");
        this.setFailedReason(reason);
    }
    // JPA lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
        if (this.currency == null) {
            this.currency = "USD";
        }
        if (this.paymentType == null) {
            this.paymentType = "MONTHLY_FEE";
        }
        if (this.status == null) {
            this.status = this.isPaid ? "PAID" : "PENDING";
        }
    }
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        // Sync status with isPaid for backward compatibility
        if (this.isPaid && ! "PAID" .equals(this.status)) {
            this.status = "PAID";
            if (this.completedAt == null) {
                this.completedAt = LocalDateTime.now();
            }
        } else
        if (!this.isPaid && "PAID" .equals(this.status)) {
            this.status = "PENDING";
        }
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Payment)) return false;
        Payment payment =(Payment) o;
        return id != null && id.equals(payment.id);
    }
    @Override
    public int hashCode() {
        return getClass() .hashCode();
    }
    @Override
    public String toString() {
        return "Payment{" + "id=" + id + ", montant=" + montant + ", mois=" + mois + ", isPaid=" + isPaid + ", status='" + status + '\'' + ", currency='" + currency + '\'' + ", paymentType='" + paymentType + '\'' + '}';
    }
}
