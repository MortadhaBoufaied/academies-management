package com.footballacademy.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**  * Sport entity for multi-sport academy management.  * Supports multiple sports (Football, Basketball, Handball, Speedball, etc.)  * with sport-specific configurations.  */
@Entity
@Table(name = "sports", indexes = {
    @Index(name = "idx_sport_theme", columnList = "theme_id")
})
public
class Sport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String code;
    @Column(nullable = false)
    private String name;
    private String description;
    @Column(nullable = false)
    private Boolean isActive;
    @Column(nullable = false)
    private Integer displayOrder;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id", foreignKey =
    @ForeignKey(name = "fk_sport_theme"))
    @JsonIgnore
    private SportTheme theme;
    // Sport-specific positions
    @OneToMany(mappedBy = "sport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SportPosition> positions = new ArrayList<>();
    // Sport-specific statistics
    @OneToMany(mappedBy = "sport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SportStatistic> statistics = new ArrayList<>();
    public Sport() {
        this.isActive = true;
        this.displayOrder = 0;
    }
    public Sport(String code, String name) {
        this();
        this.code = code;
        this.name = name;
    }
    // Getters and Setters
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
    public Boolean getIsActive() {
        return isActive;
    }
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    public Integer getDisplayOrder() {
        return displayOrder;
    }
    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
    public SportTheme getTheme() {
        return theme;
    }
    public void setTheme(SportTheme theme) {
        this.theme = theme;
    }
    public Long getThemeId() {
        return theme != null ? theme.getId() : null;
    }
    public List<SportPosition> getPositions() {
        return positions;
    }
    public void setPositions(List<SportPosition> positions) {
        this.positions = positions;
    }
    public List<SportStatistic> getStatistics() {
        return statistics;
    }
    public void setStatistics(List<SportStatistic> statistics) {
        this.statistics = statistics;
    }
    // Convenience methods
    public void addPosition(SportPosition position) {
        positions.add(position);
        position.setSport(this);
    }
    public void removePosition(SportPosition position) {
        positions.remove(position);
        position.setSport(null);
    }
    public void addStatistic(SportStatistic statistic) {
        statistics.add(statistic);
        statistic.setSport(this);
    }
    public void removeStatistic(SportStatistic statistic) {
        statistics.remove(statistic);
        statistic.setSport(null);
    }
}
