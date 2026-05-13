package com.footballacademy.model;

import jakarta.persistence.*;

@Entity
@Table(name = "sport_categories", indexes = {
    @Index(name = "idx_sport_category_code", columnList = "code"),
    @Index(name = "idx_sport_category_sport", columnList = "sport_id"),
    @Index(name = "idx_sport_category_active", columnList = "is_active")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_sport_category_code_sport", columnNames = {
        "code", "sport_id"
    })
})
public
class SportCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String code;
    @Column(nullable = false)
    private String name;
    private String description;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sport_id", foreignKey =
    @ForeignKey(name = "fk_sport_category_sport"))
    private Sport sport;
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;
    public SportCategory() {
    }
    public SportCategory(String code, String name) {
        this.code = code;
        this.name = name;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public Sport getSport() {
        return sport;
    }
    public void setSport(Sport sport) {
        this.sport = sport;
    }
    public Boolean getIsActive() {
        return isActive;
    }
    public void setIsActive(Boolean active) {
        isActive = active;
    }
    public Integer getDisplayOrder() {
        return displayOrder;
    }
    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}
