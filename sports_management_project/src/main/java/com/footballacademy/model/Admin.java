package com.footballacademy.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "admins")
public
class Admin {
    public
    enum AdminResponsibility {
        ACADEMY_DIRECTOR, OPERATIONS_MANAGER, SPORTS_COORDINATOR, PLAYER_REGISTRAR, FINANCE_MANAGER, COMMUNICATIONS_MANAGER, MEDICAL_WELFARE_MANAGER
    }
    @Id
    private Long id;
    // Shared with User
    @Enumerated(EnumType.STRING)
    private AdminResponsibility responsibility = AdminResponsibility.OPERATIONS_MANAGER;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "user_id", unique = true)
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academy_id", foreignKey =
    @ForeignKey(name = "fk_admin_academy"))
    private Academy academy;
    public Admin() {
    }
    public Admin(User user, String responsibility) {
        this.user = user;
        setResponsibility(responsibility);
    }
    public Admin(User user, AdminResponsibility responsibility) {
        this.user = user;
        this.responsibility = responsibility != null ? responsibility : AdminResponsibility.OPERATIONS_MANAGER;
    }
    public Long getId() {
        return id;
    }
    public AdminResponsibility getResponsibility() {
        return responsibility;
    }
    public void setResponsibility(AdminResponsibility responsibility) {
        this.responsibility = responsibility != null ? responsibility : AdminResponsibility.OPERATIONS_MANAGER;
    }
    public void setResponsibility(String responsibility) {
        if (responsibility == null || responsibility.isBlank()) {
            this.responsibility = AdminResponsibility.OPERATIONS_MANAGER;
            return;
        }
        try {
            this.responsibility = AdminResponsibility.valueOf(responsibility.trim() .toUpperCase());
        } catch (IllegalArgumentException ignored) {
            this.responsibility = AdminResponsibility.OPERATIONS_MANAGER;
        }
    }
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public Academy getAcademy() {
        return academy;
    }
    public void setAcademy(Academy academy) {
        this.academy = academy;
    }
}
