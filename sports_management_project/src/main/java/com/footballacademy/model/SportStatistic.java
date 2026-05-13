package com.footballacademy.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

/**  * SportStatistic entity for defining statistics specific to each sport.  * Examples: Football (goals, assists, tackles), Basketball (points, rebounds, assists), etc.  */
@Entity
@Table(name = "sport_statistics")
public
class SportStatistic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String code;
    @Column(nullable = false)
    private String name;
    private String description;
    @Column(nullable = false)
    private String dataType;
    // INTEGER, DOUBLE, BOOLEAN, STRING
    @Column(nullable = false)
    private Boolean isRequired;
    @Column(nullable = false)
    private Integer displayOrder;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sport_id", foreignKey =
    @ForeignKey(name = "fk_statistic_sport"))
    @JsonIgnore
    private Sport sport;
    public SportStatistic() {
        this.dataType = "INTEGER";
        this.isRequired = false;
        this.displayOrder = 0;
    }
    public SportStatistic(String code, String name, String dataType) {
        this();
        this.code = code;
        this.name = name;
        this.dataType = dataType;
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
    public String getDataType() {
        return dataType;
    }
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
    public Boolean getIsRequired() {
        return isRequired;
    }
    public void setIsRequired(Boolean isRequired) {
        this.isRequired = isRequired;
    }
    public Integer getDisplayOrder() {
        return displayOrder;
    }
    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
    public Sport getSport() {
        return sport;
    }
    public void setSport(Sport sport) {
        this.sport = sport;
    }
}
