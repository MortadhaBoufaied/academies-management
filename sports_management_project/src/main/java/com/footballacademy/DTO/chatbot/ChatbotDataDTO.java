package com.footballacademy.DTO.chatbot;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

public
class ChatbotDataDTO {
    private Long id;
    @NotBlank
    @Size(max = 150)
    private String intent;
    @NotBlank
    @Size(max = 500)
    private String question;
    @NotBlank
    @Size(max = 2000)
    private String answer;
    private List<String> keywords;
    private String category;
    // e.g. REGISTRATION, PAYMENTS, TRAINING, GENERAL
    private boolean active = true;
    @NotNull
    private Long academyId;
    // null or ignored for global/super-admin data
    private Instant createdAt;
    private Instant updatedAt;
    public ChatbotDataDTO() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    public ChatbotDataDTO(Long id, String intent, String question, String answer, List<String> keywords, String category, boolean active, Long academyId, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.intent = intent;
        this.question = question;
        this.answer = answer;
        this.keywords = keywords;
        this.category = category;
        this.active = active;
        this.academyId = academyId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Additional constructor for controller usage
    public ChatbotDataDTO(Long id, String question, String answer, String tags, String scope, Long academyId, Long sportId, String sourceType, Long uploadedById, String relPath, String absPath) {
        this.id = id;
        this.question = question;
        this.answer = answer;
        this.keywords = tags != null ? List.of(tags.split(",")) : List.of();
        this.category = scope;
        this.academyId = academyId;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    // ========================
    // Getters & Setters
    // ========================
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getIntent() {
        return intent;
    }
    public void setIntent(String intent) {
        this.intent = intent;
    }
    public String getQuestion() {
        return question;
    }
    public void setQuestion(String question) {
        this.question = question;
    }
    public String getAnswer() {
        return answer;
    }
    public void setAnswer(String answer) {
        this.answer = answer;
        this.updatedAt = Instant.now();
    }
    public List<String> getKeywords() {
        return keywords;
    }
    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }
    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }
    public Long getAcademyId() {
        return academyId;
    }
    public void setAcademyId(Long academyId) {
        this.academyId = academyId;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
