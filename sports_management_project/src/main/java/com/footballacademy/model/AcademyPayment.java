package com.footballacademy.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "academy_payments", indexes = {
    @Index(name = "idx_academy_payment_academy", columnList = "academy_id"),
    @Index(name = "idx_academy_payment_status", columnList = "status"),
    @Index(name = "idx_academy_payment_created", columnList = "created_at")
})
public
class AcademyPayment {
    public
    enum PaymentStatus {
        PENDING, PAID, CANCELLED
    }
    public
    enum PaymentMethod {
        CARD, BANK_TRANSFER, CASH, MANUAL
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "academy_id", nullable = false, foreignKey =
    @ForeignKey(name = "fk_academy_payment_academy"))
    @JsonIgnore
    private Academy academy;
    @Enumerated(EnumType.STRING)
    @Column(name = "offer_code", nullable = false, length = 40)
    private Academy.SubscriptionOffer offer = Academy.SubscriptionOffer.REGULAR;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;
    @Column(nullable = false, length = 3)
    private String currency = "TND";
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.PENDING;
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod = PaymentMethod.MANUAL;
    @Column(name = "reference_code", length = 100)
    private String referenceCode;
    @Column(length = 500)
    private String notes;
    @Column(name = "due_date")
    private LocalDate dueDate;
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (status == null) {
            status = PaymentStatus.PENDING;
        }
        if (paymentMethod == null) {
            paymentMethod = PaymentMethod.MANUAL;
        }
        if (currency == null || currency.isBlank()) {
            currency = "TND";
        } else {
            currency = currency.trim() .toUpperCase();
        }
        if (amount == null) {
            amount = BigDecimal.ZERO;
        }
    }
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (currency == null || currency.isBlank()) {
            currency = "TND";
        } else {
            currency = currency.trim() .toUpperCase();
        }
    }
    public void markPaid(PaymentMethod method, String extraNotes) {
        this.status = PaymentStatus.PAID;
        this.paymentMethod = method != null ? method : PaymentMethod.MANUAL;
        this.paidAt = LocalDateTime.now();
        if (extraNotes != null && !extraNotes.isBlank()) {
            this.notes = extraNotes;
        }
    }
    public boolean isPending() {
        return status == PaymentStatus.PENDING;
    }
    public boolean isPaid() {
        return status == PaymentStatus.PAID;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Academy getAcademy() {
        return academy;
    }
    public void setAcademy(Academy academy) {
        this.academy = academy;
    }
    public Academy.SubscriptionOffer getOffer() {
        return offer;
    }
    public void setOffer(Academy.SubscriptionOffer offer) {
        this.offer = offer;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    public String getCurrency() {
        return currency;
    }
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    public PaymentStatus getStatus() {
        return status;
    }
    public void setStatus(PaymentStatus status) {
        this.status = status;
    }
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    public String getReferenceCode() {
        return referenceCode;
    }
    public void setReferenceCode(String referenceCode) {
        this.referenceCode = referenceCode;
    }
    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }
    public LocalDate getDueDate() {
        return dueDate;
    }
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
    public LocalDateTime getPaidAt() {
        return paidAt;
    }
    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
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
}
