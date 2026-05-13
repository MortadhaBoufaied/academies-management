package com.footballacademy.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "academies", indexes = {
    @Index(name = "idx_academy_slug", columnList = "slug", unique = true),
    @Index(name = "idx_academy_status", columnList = "status"),
    @Index(name = "idx_academy_sport", columnList = "sport_id"),
    @Index(name = "idx_academy_city", columnList = "city")
})
public
class Academy {
    public
    enum AcademyStatus {
        ACTIVE, INACTIVE, SUSPENDED
    }
    public
    enum SubscriptionOffer {
        REGULAR, PRO
    }
    public
    enum SubscriptionPaymentStatus {
        PENDING, PAID
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, unique = true)
    private String slug;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String country;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AcademyStatus status = AcademyStatus.ACTIVE;
    @Column(name = "logo_url", length = 1000)
    private String logoUrl;
    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_offer", nullable = false, length = 40)
    private SubscriptionOffer subscriptionOffer = SubscriptionOffer.REGULAR;
    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_payment_status", nullable = false, length = 40)
    private SubscriptionPaymentStatus subscriptionPaymentStatus = SubscriptionPaymentStatus.PENDING;
    @Column(name = "subscription_activated_at")
    private LocalDateTime subscriptionActivatedAt;
    @Column(name = "subscription_updated_at")
    private LocalDateTime subscriptionUpdatedAt;
    @Column(name = "latest_performance_score")
    private Double latestPerformanceScore;
    @Column(name = "latest_ranking_position")
    private Integer latestRankingPosition;
    @Column(name = "performance_updated_at")
    private LocalDateTime performanceUpdatedAt;
    @Column(name = "scouter_contact_enabled", nullable = false)
    private Boolean scouterContactEnabled = true;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sport_id", foreignKey =
    @ForeignKey(name = "fk_academy_sport"))
    private Sport sport;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", foreignKey =
    @ForeignKey(name = "fk_academy_owner_user"))
    @JsonIgnore
    private User ownerUser;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    public Academy() {
    }
    public Academy(String name, String slug) {
        this.name = name;
        this.slug = slug;
    }
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
            status = AcademyStatus.ACTIVE;
        }
        if (subscriptionOffer == null) {
            subscriptionOffer = SubscriptionOffer.REGULAR;
        }
        if (subscriptionPaymentStatus == null) {
            subscriptionPaymentStatus = SubscriptionPaymentStatus.PENDING;
        }
        if (subscriptionUpdatedAt == null) {
            subscriptionUpdatedAt = now;
        }
        if (scouterContactEnabled == null) {
            scouterContactEnabled = true;
        }
    }
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = AcademyStatus.ACTIVE;
        }
        if (subscriptionOffer == null) {
            subscriptionOffer = SubscriptionOffer.REGULAR;
        }
        if (subscriptionPaymentStatus == null) {
            subscriptionPaymentStatus = SubscriptionPaymentStatus.PENDING;
        }
        if (subscriptionUpdatedAt == null) {
            subscriptionUpdatedAt = LocalDateTime.now();
        }
        if (scouterContactEnabled == null) {
            scouterContactEnabled = true;
        }
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getSlug() {
        return slug;
    }
    public void setSlug(String slug) {
        this.slug = slug;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }
    public String getCountry() {
        return country;
    }
    public void setCountry(String country) {
        this.country = country;
    }
    public AcademyStatus getStatus() {
        return status;
    }
    public void setStatus(AcademyStatus status) {
        this.status = status;
    }
    public String getLogoUrl() {
        return logoUrl;
    }
    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }
    public SubscriptionOffer getSubscriptionOffer() {
        return subscriptionOffer;
    }
    public void setSubscriptionOffer(SubscriptionOffer subscriptionOffer) {
        this.subscriptionOffer = subscriptionOffer;
    }
    public SubscriptionPaymentStatus getSubscriptionPaymentStatus() {
        return subscriptionPaymentStatus;
    }
    public void setSubscriptionPaymentStatus(SubscriptionPaymentStatus subscriptionPaymentStatus) {
        this.subscriptionPaymentStatus = subscriptionPaymentStatus;
    }
    public LocalDateTime getSubscriptionActivatedAt() {
        return subscriptionActivatedAt;
    }
    public void setSubscriptionActivatedAt(LocalDateTime subscriptionActivatedAt) {
        this.subscriptionActivatedAt = subscriptionActivatedAt;
    }
    public LocalDateTime getSubscriptionUpdatedAt() {
        return subscriptionUpdatedAt;
    }
    public void setSubscriptionUpdatedAt(LocalDateTime subscriptionUpdatedAt) {
        this.subscriptionUpdatedAt = subscriptionUpdatedAt;
    }
    public Double getLatestPerformanceScore() {
        return latestPerformanceScore;
    }
    public void setLatestPerformanceScore(Double latestPerformanceScore) {
        this.latestPerformanceScore = latestPerformanceScore;
    }
    public Integer getLatestRankingPosition() {
        return latestRankingPosition;
    }
    public void setLatestRankingPosition(Integer latestRankingPosition) {
        this.latestRankingPosition = latestRankingPosition;
    }
    public LocalDateTime getPerformanceUpdatedAt() {
        return performanceUpdatedAt;
    }
    public void setPerformanceUpdatedAt(LocalDateTime performanceUpdatedAt) {
        this.performanceUpdatedAt = performanceUpdatedAt;
    }
    public Boolean getScouterContactEnabled() {
        return scouterContactEnabled;
    }
    public void setScouterContactEnabled(Boolean scouterContactEnabled) {
        this.scouterContactEnabled = scouterContactEnabled != null ? scouterContactEnabled : true;
    }
    public Sport getSport() {
        return sport;
    }
    public void setSport(Sport sport) {
        this.sport = sport;
    }
    public Long getSportId() {
        return sport != null ? sport.getId() : null;
    }
    public User getOwnerUser() {
        return ownerUser;
    }
    public void setOwnerUser(User ownerUser) {
        this.ownerUser = ownerUser;
    }
    public Set<Sport> getActiveSports() {
        return sport == null ? Collections.emptySet() : Collections.singleton(sport);
    }
    public void setActiveSports(Set<Sport> activeSports) {
        if (activeSports == null || activeSports.isEmpty()) {
            this.sport = null;
            return;
        } this.sport = new LinkedHashSet<>(activeSports) .iterator() .next();
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
