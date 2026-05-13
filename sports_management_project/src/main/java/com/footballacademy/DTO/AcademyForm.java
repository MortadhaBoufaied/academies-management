package com.footballacademy.DTO;

import com.footballacademy.model.Academy;
import com.footballacademy.model.User;

public class AcademyForm {

    private Long id;
    private String name;
    private String slug;
    private Academy.AcademyStatus status = Academy.AcademyStatus.ACTIVE;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String country;
    private String logoUrl;
    private Long sportId;
    private Academy.SubscriptionOffer subscriptionOffer = Academy.SubscriptionOffer.REGULAR;
    private Academy.SubscriptionPaymentStatus subscriptionPaymentStatus =
            Academy.SubscriptionPaymentStatus.PENDING;

    private String ownerName;
    private String ownerPassword;
    private String ownerPhone;

    // =====================================================
    // === FACTORIES
    // =====================================================

    public static AcademyForm from(Academy academy, User ownerUser) {
        AcademyForm form = new AcademyForm();

        if (academy == null) {
            return form;
        }

        form.setId(academy.getId());
        form.setName(academy.getName());
        form.setSlug(academy.getSlug());
        form.setStatus(academy.getStatus());
        form.setEmail(academy.getEmail());
        form.setPhone(academy.getPhone());
        form.setAddress(academy.getAddress());
        form.setCity(academy.getCity());
        form.setCountry(academy.getCountry());
        form.setLogoUrl(academy.getLogoUrl());
        form.setSportId(academy.getSportId());
        form.setSubscriptionOffer(academy.getSubscriptionOffer());
        form.setSubscriptionPaymentStatus(academy.getSubscriptionPaymentStatus());

        if (ownerUser != null) {
            form.setOwnerName(ownerUser.getNom());
            form.setOwnerPhone(ownerUser.getTel());
        }

        return form;
    }

    // =====================================================
    // === TO ENTITY
    // =====================================================

    public Academy toAcademy() {
        Academy academy = new Academy();

        academy.setId(id);
        academy.setName(name);
        academy.setSlug(slug);
        academy.setStatus(status);
        academy.setEmail(email);
        academy.setPhone(phone);
        academy.setAddress(address);
        academy.setCity(city);
        academy.setCountry(country);
        academy.setLogoUrl(logoUrl);
        academy.setSubscriptionOffer(subscriptionOffer);
        academy.setSubscriptionPaymentStatus(subscriptionPaymentStatus);

        return academy;
    }

    public boolean isCreateMode() {
        return id == null;
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

    public Academy.AcademyStatus getStatus() {
        return status;
    }

    public void setStatus(Academy.AcademyStatus status) {
        this.status = status;
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

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public Long getSportId() {
        return sportId;
    }

    public void setSportId(Long sportId) {
        this.sportId = sportId;
    }

    public Academy.SubscriptionOffer getSubscriptionOffer() {
        return subscriptionOffer;
    }

    public void setSubscriptionOffer(Academy.SubscriptionOffer subscriptionOffer) {
        this.subscriptionOffer = subscriptionOffer;
    }

    public Academy.SubscriptionPaymentStatus getSubscriptionPaymentStatus() {
        return subscriptionPaymentStatus;
    }

    public void setSubscriptionPaymentStatus(
            Academy.SubscriptionPaymentStatus subscriptionPaymentStatus) {
        this.subscriptionPaymentStatus = subscriptionPaymentStatus;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerPassword() {
        return ownerPassword;
    }

    public void setOwnerPassword(String ownerPassword) {
        this.ownerPassword = ownerPassword;
    }

    public String getOwnerPhone() {
        return ownerPhone;
    }

    public void setOwnerPhone(String ownerPhone) {
        this.ownerPhone = ownerPhone;
    }
}
