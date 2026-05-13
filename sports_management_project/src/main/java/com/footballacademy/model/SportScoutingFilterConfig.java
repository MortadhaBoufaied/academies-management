package com.footballacademy.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sport_scouting_filter_configs", indexes = {
    @Index(name = "idx_scouting_filter_sport", columnList = "sport_id"),
    @Index(name = "idx_scouting_filter_key", columnList = "filter_key"),
    @Index(name = "idx_scouting_filter_active", columnList = "is_active"),
    @Index(name = "idx_scouting_filter_order", columnList = "display_order")
})
public class SportScoutingFilterConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sport_id", foreignKey = @ForeignKey(name = "fk_scouting_filter_sport"))
    @JsonIgnore
    private Sport sport;

    @Column(name = "filter_key", nullable = false, length = 120)
    private String filterKey;

    @Column(name = "filter_label", nullable = false, length = 160)
    private String filterLabel;

    @Column(name = "filter_type", nullable = false, length = 40)
    private String filterType = "SELECT";

    @Column(name = "allowed_values", columnDefinition = "TEXT")
    private String allowedValues;

    @Column(name = "min_value")
    private Double minValue;

    @Column(name = "max_value")
    private Double maxValue;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (active == null) active = true;
        if (displayOrder == null) displayOrder = 0;
        if (filterType == null || filterType.isBlank()) filterType = "SELECT";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (active == null) active = true;
        if (displayOrder == null) displayOrder = 0;
        if (filterType == null || filterType.isBlank()) filterType = "SELECT";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Sport getSport() { return sport; }
    public void setSport(Sport sport) { this.sport = sport; }
    public Long getSportId() { return sport != null ? sport.getId() : null; }
    public String getFilterKey() { return filterKey; }
    public void setFilterKey(String filterKey) { this.filterKey = filterKey; }
    public String getFilterLabel() { return filterLabel; }
    public void setFilterLabel(String filterLabel) { this.filterLabel = filterLabel; }
    public String getFilterType() { return filterType; }
    public void setFilterType(String filterType) { this.filterType = filterType != null ? filterType.trim().toUpperCase() : "SELECT"; }
    public String getAllowedValues() { return allowedValues; }
    public void setAllowedValues(String allowedValues) { this.allowedValues = allowedValues; }
    public Double getMinValue() { return minValue; }
    public void setMinValue(Double minValue) { this.minValue = minValue; }
    public Double getMaxValue() { return maxValue; }
    public void setMaxValue(Double maxValue) { this.maxValue = maxValue; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
