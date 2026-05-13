package com.footballacademy.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sport_themes", indexes = {
    @Index(name = "idx_sport_theme_scope", columnList = "scope"),
    @Index(name = "idx_sport_theme_academy", columnList = "academy_id"),
    @Index(name = "idx_sport_theme_sport", columnList = "sport_id")
})
public
class SportTheme {
    public
    enum ThemeScope {
        PLATFORM_DEFAULT, GLOBAL, ACADEMY
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ThemeScope scope = ThemeScope.GLOBAL;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academy_id", foreignKey =
    @ForeignKey(name = "fk_sport_theme_academy"))
    private Academy academy;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sport_id", foreignKey =
    @ForeignKey(name = "fk_sport_theme_sport"))
    private Sport sport;
    private String primaryColor;
    private String secondaryColor;
    private String backgroundColor;
    private String accentColor;
    private String textColor;
    @Column(length = 1000)
    private String logoUrl;
    @Column(length = 1000)
    private String defaultPlayerImageUrl;
    @Column(length = 1000)
    private String defaultTrainerImageUrl;
    @Column(length = 1000)
    private String defaultParentImageUrl;
    @Column(length = 1000)
    private String defaultAdminImageUrl;
    @Column(length = 1000)
    private String homeBannerUrl;
    @Column(length = 1000)
    private String splashImageUrl;
    private String cardStyle;
    private String fontFamily;
    private String buttonStyle;
    private String iconStyle;
    @Column(nullable = false)
    private Integer version = 1;
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    public SportTheme() {
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
        if (version == null) {
            version = 1;
        }
        if (scope == null) {
            scope = ThemeScope.GLOBAL;
        }
    }
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (version == null) {
            version = 1;
        }
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public ThemeScope getScope() {
        return scope;
    }
    public void setScope(ThemeScope scope) {
        this.scope = scope;
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
    public String getPrimaryColor() {
        return primaryColor;
    }
    public void setPrimaryColor(String primaryColor) {
        this.primaryColor = primaryColor;
    }
    public String getSecondaryColor() {
        return secondaryColor;
    }
    public void setSecondaryColor(String secondaryColor) {
        this.secondaryColor = secondaryColor;
    }
    public String getBackgroundColor() {
        return backgroundColor;
    }
    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
    public String getAccentColor() {
        return accentColor;
    }
    public void setAccentColor(String accentColor) {
        this.accentColor = accentColor;
    }
    public String getTextColor() {
        return textColor;
    }
    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }
    public String getLogoUrl() {
        return logoUrl;
    }
    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }
    public String getDefaultPlayerImageUrl() {
        return defaultPlayerImageUrl;
    }
    public void setDefaultPlayerImageUrl(String defaultPlayerImageUrl) {
        this.defaultPlayerImageUrl = defaultPlayerImageUrl;
    }
    public String getDefaultTrainerImageUrl() {
        return defaultTrainerImageUrl;
    }
    public void setDefaultTrainerImageUrl(String defaultTrainerImageUrl) {
        this.defaultTrainerImageUrl = defaultTrainerImageUrl;
    }
    public String getDefaultParentImageUrl() {
        return defaultParentImageUrl;
    }
    public void setDefaultParentImageUrl(String defaultParentImageUrl) {
        this.defaultParentImageUrl = defaultParentImageUrl;
    }
    public String getDefaultAdminImageUrl() {
        return defaultAdminImageUrl;
    }
    public void setDefaultAdminImageUrl(String defaultAdminImageUrl) {
        this.defaultAdminImageUrl = defaultAdminImageUrl;
    }
    public String getHomeBannerUrl() {
        return homeBannerUrl;
    }
    public void setHomeBannerUrl(String homeBannerUrl) {
        this.homeBannerUrl = homeBannerUrl;
    }
    public String getSplashImageUrl() {
        return splashImageUrl;
    }
    public void setSplashImageUrl(String splashImageUrl) {
        this.splashImageUrl = splashImageUrl;
    }
    public String getCardStyle() {
        return cardStyle;
    }
    public void setCardStyle(String cardStyle) {
        this.cardStyle = cardStyle;
    }
    public String getFontFamily() {
        return fontFamily;
    }
    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
    }
    public String getButtonStyle() {
        return buttonStyle;
    }
    public void setButtonStyle(String buttonStyle) {
        this.buttonStyle = buttonStyle;
    }
    public String getIconStyle() {
        return iconStyle;
    }
    public void setIconStyle(String iconStyle) {
        this.iconStyle = iconStyle;
    }
    public Integer getVersion() {
        return version;
    }
    public void setVersion(Integer version) {
        this.version = version;
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
