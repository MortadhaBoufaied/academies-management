package com.footballacademy.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_user_email", columnList = "email", unique = true),
                @Index(name = "idx_user_active", columnList = "active"),
                @Index(name = "idx_user_main_role", columnList = "main_role"),
                @Index(name = "idx_user_academy", columnList = "academy_id"),
                @Index(name = "idx_user_last_login", columnList = "last_login"),
                @Index(name = "idx_user_registration_date", columnList = "registration_date"),
                @Index(name = "idx_user_nom", columnList = "nom")
        }
)
public class User {

    // =====================================================
    // === PRIMARY KEY
    // =====================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =====================================================
    // === PERSONAL INFORMATION
    // =====================================================

    private String nom;

    @Column(name = "date_naissance")
    private LocalDate dateNaiss;

    private String tel;

    @Column(nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String mdp;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(length = 1000)
    private String bio;

    @Column(name = "profile_photo_url", length = 500)
    private String profilePhotoUrl;

    // =====================================================
    // === ACTIVITY / AUDIT
    // =====================================================

    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "login_count")
    private Long loginCount = 0L;

    // =====================================================
    // === ROLE MANAGEMENT
    // =====================================================

    @Enumerated(EnumType.STRING)
    @Column(name = "main_role", nullable = false)
    private UserRole mainRole;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    // =====================================================
    // === RELATIONSHIPS
    // =====================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "academy_id",
            foreignKey = @ForeignKey(name = "fk_user_academy")
    )
    @JsonIgnore
    private Academy academy;

    // =====================================================
    // === ENUMS
    // =====================================================

    public enum UserRole {
        SUPER_ADMIN,
        ADMIN,
        PLAYER,
        PARENT,
        TRAINER,
        SCOUTER
    }

    // =====================================================
    // === CONSTRUCTORS
    // =====================================================

    public User() {
    }

    public User(
            String nom,
            LocalDate dateNaiss,
            String tel,
            String email,
            String mdp,
            UserRole mainRole
    ) {
        this.nom = nom;
        this.dateNaiss = dateNaiss;
        this.tel = tel;
        this.email = email;
        this.mdp = mdp;
        this.mainRole = mainRole;
    }

    // =====================================================
    // === JPA LIFECYCLE
    // =====================================================

    @PrePersist
    protected void onCreate() {
        if (active == null) {
            active = true;
        }
        if (registrationDate == null) {
            registrationDate = LocalDateTime.now();
        }
        if (loginCount == null) {
            loginCount = 0L;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (loginCount == null) {
            loginCount = 0L;
        }
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

    public LocalDate getDateNaiss() {
        return dateNaiss;
    }

    public void setDateNaiss(LocalDate dateNaiss) {
        this.dateNaiss = dateNaiss;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMdp() {
        return mdp;
    }

    public void setMdp(String mdp) {
        this.mdp = mdp;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfilePhotoUrl() {
        return profilePhotoUrl;
    }

    public void setProfilePhotoUrl(String profilePhotoUrl) {
        this.profilePhotoUrl = profilePhotoUrl;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Long getLoginCount() {
        return loginCount;
    }

    public void setLoginCount(Long loginCount) {
        this.loginCount = loginCount;
    }

    public UserRole getMainRole() {
        return mainRole;
    }

    public void setMainRole(UserRole mainRole) {
        this.mainRole = mainRole;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }

    public Academy getAcademy() {
        return academy;
    }

    public void setAcademy(Academy academy) {
        this.academy = academy;
    }

    public Long getAcademyId() {
        return academy != null ? academy.getId() : null;
    }

    // =====================================================
    // === ROLE UTILITIES
    // =====================================================

    /**
     * Convenience check for roles.
     * Supports both mainRole enum and mapped Role entities.
     */
    public boolean hasRole(String roleName) {
        if (roleName == null) {
            return false;
        }

        String normalized = roleName.trim().toUpperCase();
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring(5);
        }

        if (mainRole != null && mainRole.name().equalsIgnoreCase(normalized)) {
            return true;
        }

        for (Role role : roles) {
            if (role != null && role.getName() != null) {
                String name = role.getName().trim().toUpperCase();
                if (name.startsWith("ROLE_")) {
                    name = name.substring(5);
                }
                if (name.equalsIgnoreCase(normalized)) {
                    return true;
                }
            }
        }

        return false;
    }
}
