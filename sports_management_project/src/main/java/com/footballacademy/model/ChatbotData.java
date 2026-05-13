package com.footballacademy.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chatbot_data", indexes = {
    @Index(name = "idx_chatbot_scope", columnList = "scope"),
    @Index(name = "idx_chatbot_academy", columnList = "academy_id"),
    @Index(name = "idx_chatbot_sport", columnList = "sport_id"),
    @Index(name = "idx_chatbot_source_type", columnList = "source_type")
})
public
class ChatbotData {
    public
    enum Scope {
        GLOBAL, ACADEMY
    }
    public
    enum SourceType {
        CSV, MANUAL, DATABASE
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Scope scope = Scope.GLOBAL;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academy_id", foreignKey =
    @ForeignKey(name = "fk_chatbot_data_academy"))
    private Academy academy;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sport_id", foreignKey =
    @ForeignKey(name = "fk_chatbot_data_sport"))
    private Sport sport;
    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private SourceType sourceType = SourceType.MANUAL;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", foreignKey =
    @ForeignKey(name = "fk_chatbot_data_uploaded_by"))
    private User uploadedBy;
    @Column(nullable = false, length = 2000)
    private String question;
    @Column(nullable = false, length = 10000)
    private String answer;
    @Column(length = 1000)
    private String tags;
    /* =======================        FILE METADATA (ALL REQUIRED BY DB)        ======================= */
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;
    @Column(name = "file_path", nullable = false, length = 2000)
    private String filePath;
    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;
    /* =======================        AUTO-POPULATE REQUIRED FIELDS        ======================= */
    @PrePersist
    protected void onCreate() {
        if (uploadedAt == null) {
            uploadedAt = LocalDateTime.now();
        }
        if (fileName == null || fileName.isBlank()) {
            fileName = "knowledge_base";
        }
        if (filePath == null || filePath.isBlank()) {
            filePath = "/chatbotFiles/knowledge_base.csv";
        }
        if (scope == null) {
            scope = Scope.GLOBAL;
        }
        if (sourceType == null) {
            sourceType = SourceType.MANUAL;
        }
    }
    /* =======================        GETTERS & SETTERS        ======================= */
    public Long getId() {
        return id;
    }
    public Scope getScope() {
        return scope;
    }
    public void setScope(Scope scope) {
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
    public SourceType getSourceType() {
        return sourceType;
    }
    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }
    public User getUploadedBy() {
        return uploadedBy;
    }
    public void setUploadedBy(User uploadedBy) {
        this.uploadedBy = uploadedBy;
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
    }
    public String getTags() {
        return tags;
    }
    public void setTags(String tags) {
        this.tags = tags;
    }
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public String getFilePath() {
        return filePath;
    }
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }
}
