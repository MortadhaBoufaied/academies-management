package com.footballacademy.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Division entity.
 *
 * Recommendation:
 * - Division does NOT cascade remove players/trainers
 *   (we don't want deleting a division to delete users).
 * - Player and Trainer own the relationship (@ManyToOne) via division_id.
 */
@Entity
@Table(
        name = "divisions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_division_sport_nom_categorie",
                columnNames = {"sport_id", "nom", "categorie"}
        ),
        indexes = {
                @Index(name = "idx_division_academy", columnList = "academy_id"),
                @Index(name = "idx_division_sport_id", columnList = "sport_id"),
                @Index(name = "idx_division_category_id", columnList = "category_id"),
                @Index(name = "idx_division_categorie", columnList = "categorie"),
                @Index(name = "idx_division_nom", columnList = "nom")
        }
)
public class Division {

    // =====================================================
    // === PRIMARY KEY
    // =====================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =====================================================
    // === CORE ATTRIBUTES
    // =====================================================

    @Column(nullable = false)
    private String nom;

    private String categorie;

    @Column(name = "min_age")
    private Integer minAge;

    @Column(name = "max_age")
    private Integer maxAge;

    private String gender;

    private String level;

    @Column(name = "min_weight", precision = 6, scale = 2)
    private BigDecimal minWeight;

    @Column(name = "max_weight", precision = 6, scale = 2)
    private BigDecimal maxWeight;

    @Column(name = "competition_scope")
    private String competitionScope;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    // =====================================================
    // === RELATIONSHIPS
    // =====================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "academy_id",
            foreignKey = @ForeignKey(name = "fk_division_academy")
    )
    private Academy academy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "sport_id",
            foreignKey = @ForeignKey(name = "fk_division_sport")
    )
    private Sport sport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "category_id",
            foreignKey = @ForeignKey(name = "fk_division_sport_category")
    )
    private SportCategory category;

    /**
     * Not owner side â€” no cascade remove.
     * Fetch LAZY to avoid heavy loads.
     */
    @OneToMany(mappedBy = "division", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Player> players = new ArrayList<>();

    @OneToMany(mappedBy = "division", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Trainer> trainers = new ArrayList<>();

    // =====================================================
    // === CONSTRUCTORS
    // =====================================================

    public Division() {
    }

    public Division(String nom, String categorie) {
        this.nom = nom;
        this.categorie = categorie;
    }

    // =====================================================
    // === GETTERS & SETTERS
    // =====================================================

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

    public Integer getMinAge() {
        return minAge;
    }

    public void setMinAge(Integer minAge) {
        this.minAge = minAge;
    }

    public Integer getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(Integer maxAge) {
        this.maxAge = maxAge;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public BigDecimal getMinWeight() {
        return minWeight;
    }

    public void setMinWeight(BigDecimal minWeight) {
        this.minWeight = minWeight;
    }

    public BigDecimal getMaxWeight() {
        return maxWeight;
    }

    public void setMaxWeight(BigDecimal maxWeight) {
        this.maxWeight = maxWeight;
    }

    public String getCompetitionScope() {
        return competitionScope;
    }

    public void setCompetitionScope(String competitionScope) {
        this.competitionScope = competitionScope;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Academy getAcademy() {
        return academy;
    }

    public void setAcademy(Academy academy) {
        this.academy = academy;
    }

    public Sport getSport() {
        return sport;
    }

    public void setSport(Sport sport) {
        this.sport = sport;
    }

    public SportCategory getCategory() {
        return category;
    }

    public void setCategory(SportCategory category) {
        this.category = category;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public List<Trainer> getTrainers() {
        return trainers;
    }

    // =====================================================
    // === CONVENIENCE HELPERS (OPTIONAL)
    // =====================================================

    public void addPlayer(Player player) {
        if (player == null) return;
        players.add(player);
        player.setDivision(this);
    }

    public void removePlayer(Player player) {
        if (player == null) return;
        players.remove(player);
        player.setDivision(null);
    }

    public void addTrainer(Trainer trainer) {
        if (trainer == null) return;
        trainers.add(trainer);
        trainer.setDivision(this);
    }

    public void removeTrainer(Trainer trainer) {
        if (trainer == null) return;
        trainers.remove(trainer);
        trainer.setDivision(null);
    }

    // =====================================================
    // === JPA LIFECYCLE
    // =====================================================

    /**
     * Nullify references before deletion so DB FK constraints do not block delete.
     *
     * Note:
     * This runs inside the JPA lifecycle. In complex cases,
     * handling this explicitly in a DivisionService is recommended.
     */
    @PreRemove
    protected void preRemove() {
        for (Player p : new ArrayList<>(players)) {
            p.setDivision(null);
        }
        for (Trainer t : new ArrayList<>(trainers)) {
            t.setDivision(null);
        }
    }
}
