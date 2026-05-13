package com.footballacademy.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "academy_info", indexes = {
    @Index(name = "idx_academy_info_academy", columnList = "academy_id")
})
public
class AcademyInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /* BASIC INFO */
    @Column(nullable = false)
    private String nom;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academy_id", foreignKey =
    @ForeignKey(name = "fk_academy_info_academy"))
    private Academy academy;
    @Column(columnDefinition = "TEXT")
    private String description;
    private int totalPlayers;
    private int totalCoaches;
    @Column(columnDefinition = "TEXT")
    private String topPlayers;
    @Column(columnDefinition = "TEXT")
    private String achievements;
    @Column(name = "image_url")
    private String imageUrl;
    /* CONTACT INFO */
    private String email;
    private String phone;
    private String fax;
    private String website;
    /* ADDRESS */
    private String address;
    private String city;
    private String country;
    private String postalCode;
    @Column(columnDefinition = "TEXT")
    private String googleMapsUrl;
    /* SOCIAL MEDIA */
    private String facebook;
    private String instagram;
    private String youtube;
    private String tiktok;
    /* EXTRA INFO */
    private Integer foundedYear;
    @Column(columnDefinition = "TEXT")
    private String slogan;
    @Column(columnDefinition = "TEXT")
    private String mission;
    @Column(columnDefinition = "TEXT")
    private String vision;
    /* SUPPORT CONTACT */
    private String emailSupport;
    private String phoneSupport;
    /* DIVISIONS */
    @ElementCollection
    @CollectionTable(name = "academy_info_divisions", joinColumns =
    @JoinColumn(name = "academy_info_id"))
    @Column(name = "division_ids", nullable = false)
    private List<Long> divisionsList = new ArrayList<>();
    /* CONSTRUCTORS */
    public AcademyInfo() {
    }
    /* GETTERS & SETTERS */
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
    public Academy getAcademy() {
        return academy;
    }
    public void setAcademy(Academy academy) {
        this.academy = academy;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public int getTotalPlayers() {
        return totalPlayers;
    }
    public void setTotalPlayers(int totalPlayers) {
        this.totalPlayers = totalPlayers;
    }
    public int getTotalCoaches() {
        return totalCoaches;
    }
    public void setTotalCoaches(int totalCoaches) {
        this.totalCoaches = totalCoaches;
    }
    public String getTopPlayers() {
        return topPlayers;
    }
    public void setTopPlayers(String topPlayers) {
        this.topPlayers = topPlayers;
    }
    public String getAchievements() {
        return achievements;
    }
    public void setAchievements(String achievements) {
        this.achievements = achievements;
    }
    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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
    public String getFax() {
        return fax;
    }
    public void setFax(String fax) {
        this.fax = fax;
    }
    public String getWebsite() {
        return website;
    }
    public void setWebsite(String website) {
        this.website = website;
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
    public String getPostalCode() {
        return postalCode;
    }
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
    public String getGoogleMapsUrl() {
        return googleMapsUrl;
    }
    public void setGoogleMapsUrl(String googleMapsUrl) {
        this.googleMapsUrl = googleMapsUrl;
    }
    public String getFacebook() {
        return facebook;
    }
    public void setFacebook(String facebook) {
        this.facebook = facebook;
    }
    public String getInstagram() {
        return instagram;
    }
    public void setInstagram(String instagram) {
        this.instagram = instagram;
    }
    public String getYoutube() {
        return youtube;
    }
    public void setYoutube(String youtube) {
        this.youtube = youtube;
    }
    public String getTiktok() {
        return tiktok;
    }
    public void setTiktok(String tiktok) {
        this.tiktok = tiktok;
    }
    public Integer getFoundedYear() {
        return foundedYear;
    }
    public void setFoundedYear(Integer foundedYear) {
        this.foundedYear = foundedYear;
    }
    public String getSlogan() {
        return slogan;
    }
    public void setSlogan(String slogan) {
        this.slogan = slogan;
    }
    public String getMission() {
        return mission;
    }
    public void setMission(String mission) {
        this.mission = mission;
    }
    public String getVision() {
        return vision;
    }
    public void setVision(String vision) {
        this.vision = vision;
    }
    public String getEmailSupport() {
        return emailSupport;
    }
    public void setEmailSupport(String emailSupport) {
        this.emailSupport = emailSupport;
    }
    public String getPhoneSupport() {
        return phoneSupport;
    }
    public void setPhoneSupport(String phoneSupport) {
        this.phoneSupport = phoneSupport;
    }
    public List<Long> getDivisionsList() {
        return divisionsList;
    }
    public void setDivisionsList(List<Long> divisionsList) {
        this.divisionsList = divisionsList;
    }
}
