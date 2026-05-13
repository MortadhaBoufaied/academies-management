package com.footballacademy.controllers_rest.superadmin;

import com.footballacademy.model.Academy;
import com.footballacademy.model.AcademyPayment;
import com.footballacademy.model.User;
import com.footballacademy.model.WebhookLog;
import com.footballacademy.repository.AcademyRepository;
import com.footballacademy.repository.ChatbotDataRepository;
import com.footballacademy.repository.SportCategoryRepository;
import com.footballacademy.repository.SportRepository;
import com.footballacademy.repository.SportThemeRepository;
import com.footballacademy.repository.UserRepository;
import com.footballacademy.repository.WebhookLogRepository;
import com.footballacademy.repository.WebhookRepository;
import com.footballacademy.services.academy.AcademyService;
import com.footballacademy.services.academy.AcademySubscriptionService;
import com.footballacademy.services.chatbot.ChatbotDataService;
import com.footballacademy.services.webhook.WebhookService;
import com.footballacademy.util.DataRetentionPolicy;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/super-admin/mobile")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public
class SuperAdminMobileController {
    private final AcademyService academyService;
    private final AcademySubscriptionService academySubscriptionService;
    private final ChatbotDataService chatbotDataService;
    private final WebhookService webhookService;
    private final AcademyRepository academyRepository;
    private final SportRepository sportRepository;
    private final SportCategoryRepository sportCategoryRepository;
    private final SportThemeRepository sportThemeRepository;
    private final UserRepository userRepository;
    private final ChatbotDataRepository chatbotDataRepository;
    private final WebhookRepository webhookRepository;
    private final WebhookLogRepository webhookLogRepository;
    public SuperAdminMobileController(AcademyService academyService, AcademySubscriptionService academySubscriptionService, ChatbotDataService chatbotDataService, WebhookService webhookService, AcademyRepository academyRepository, SportRepository sportRepository, SportCategoryRepository sportCategoryRepository, SportThemeRepository sportThemeRepository, UserRepository userRepository, ChatbotDataRepository chatbotDataRepository, WebhookRepository webhookRepository, WebhookLogRepository webhookLogRepository) {
        this.academyService = academyService;
        this.academySubscriptionService = academySubscriptionService;
        this.chatbotDataService = chatbotDataService;
        this.webhookService = webhookService;
        this.academyRepository = academyRepository;
        this.sportRepository = sportRepository;
        this.sportCategoryRepository = sportCategoryRepository;
        this.sportThemeRepository = sportThemeRepository;
        this.userRepository = userRepository;
        this.chatbotDataRepository = chatbotDataRepository;
        this.webhookRepository = webhookRepository;
        this.webhookLogRepository = webhookLogRepository;
    }
    public
    record UserContactDto(Long id, String name, String email, String phone, String role, Long academyId, String academyName, Boolean active) {
    }
    public record AcademyContactDto(Long academyId, String academyName, String city, String country, UserContactDto ownerUser, List<UserContactDto> admins) {
    }
    public record AcademyPaymentDto(Long id, Long academyId, String academyName, String offer, BigDecimal amount, String currency, String status, String paymentMethod, String referenceCode, String notes, LocalDate dueDate, LocalDateTime paidAt, LocalDateTime createdAt) {
    }
    public record WebhookLogDto(Long id, String webhookName, String eventType, Integer statusCode, Boolean success, String errorMessage, LocalDateTime executedAt, Long responseTimeMs) {
    }
    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("academiesCount", academyRepository.count());
        response.put("sportsCount", sportRepository.count());
        response.put("categoriesCount", sportCategoryRepository.count());
        response.put("themesCount", sportThemeRepository.count());
        response.put("usersCount", userRepository.count());
        response.put("chatbotCount", chatbotDataRepository.count());
        response.put("webhooksCount", webhookRepository.count());
        response.put("adminContactsCount", userRepository.findByMainRole(User.UserRole.ADMIN) .size());
        response.put("recentWebhookLogs", webhookLogRepository.findAll() .stream() .sorted(Comparator.comparing(WebhookLog::getExecutedAt, Comparator.nullsLast(Comparator.reverseOrder()))) .limit(10) .map(this::toWebhookLogDto) .toList());
        response.put("services", List.of(Map.of("key", "academies", "label", "Academies", "description", "Create and manage academy organizations."), Map.of("key", "sports", "label", "Sports", "description", "Maintain the sports catalog and sport-specific branding."), Map.of("key", "contact-admins", "label", "Contact Admins", "description", "Reach academy owners and administrators."), Map.of("key", "app-data", "label", "App Data", "description", "Audit global platform data and chatbot assets."), Map.of("key", "chatbot-global", "label", "Global Chatbot", "description", "Manage platform-level assistant knowledge."), Map.of("key", "webhooks", "label", "Webhooks", "description", "Control automation endpoints and execution logs."), Map.of("key", "academy-payments", "label", "Academy Payments", "description", "Review and approve academy subscriptions."), Map.of("key", "settings", "label", "Settings", "description", "Review retention and platform policies.")));
        return ResponseEntity.ok(response);
    }
    @GetMapping("/admin-contacts")
    public ResponseEntity<List<AcademyContactDto>> adminContacts() {
        List<AcademyContactDto> contacts = academyService.findAll() .stream() .sorted(Comparator.comparing(Academy::getName, String.CASE_INSENSITIVE_ORDER)) .map(academy -> new AcademyContactDto(academy.getId(), academy.getName(), academy.getCity(), academy.getCountry(), academyService.findOwnerUser(academy.getId()) .map(this::toUserContactDto) .orElse(null), userRepository.findByAcademy_IdAndMainRole(academy.getId(), User.UserRole.ADMIN) .stream() .sorted(Comparator.comparing(User::getNom, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))) .map(this::toUserContactDto) .toList())) .toList();
        return ResponseEntity.ok(contacts);
    }
    @GetMapping("/academy-payments")
    public ResponseEntity<?> academyPayments() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("payments", academySubscriptionService.allPayments() .stream() .map(this::toPaymentDto) .toList());
        response.put("pendingPaymentsCount", academySubscriptionService.pendingPaymentsCount());
        response.put("totalCollected", academySubscriptionService.totalCollected());
        response.put("currency", academySubscriptionService.currency());
        response.put("offerPrices", academySubscriptionService.offerPrices());
        return ResponseEntity.ok(response);
    }
    @PostMapping("/academy-payments/{id}/mark-paid")
    public ResponseEntity<?> markAcademyPaymentPaid(
    @PathVariable Long id,
    @RequestBody(required = false) Map<String, Object> body) {
        AcademyPayment.PaymentMethod method = AcademyPayment.PaymentMethod.MANUAL;
        String notes = "Approved from mobile super-admin portal";
        if (body != null) {
            Object methodRaw = body.get("paymentMethod");
            if (methodRaw != null) {
                method = AcademyPayment.PaymentMethod.valueOf(String.valueOf(methodRaw) .trim() .toUpperCase());
            }
            Object notesRaw = body.get("notes");
            if (notesRaw != null && !String.valueOf(notesRaw) .isBlank()) {
                notes = String.valueOf(notesRaw);
            }
        }
        return ResponseEntity.ok(toPaymentDto(academySubscriptionService.markPaymentPaid(id, method, notes)));
    }
    @GetMapping("/app-data")
    public ResponseEntity<?> appData() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("knowledgeBaseExists", chatbotDataService.knowledgeBaseExists());
        response.put("knowledgeBaseWebPath", chatbotDataService.knowledgeBaseWebPath());
        response.put("knowledgeBaseServerPath", chatbotDataService.knowledgeBaseServerPath());
        response.put("academiesCount", academyRepository.count());
        response.put("sportsCount", sportRepository.count());
        response.put("usersCount", userRepository.count());
        response.put("chatbotCount", chatbotDataRepository.count());
        response.put("webhooksCount", webhookRepository.count());
        return ResponseEntity.ok(response);
    }
    @GetMapping("/settings")
    public ResponseEntity<?> settings() {
        return ResponseEntity.ok(Map.of("retentionPolicies", DataRetentionPolicy.getAllPolicies(), "currency", academySubscriptionService.currency(), "offerPrices", academySubscriptionService.offerPrices()));
    }
    @GetMapping("/webhook-logs/failed")
    public ResponseEntity<List<WebhookLogDto>> failedWebhookLogs() {
        return ResponseEntity.ok(webhookService.getFailedWebhookLogs() .stream() .limit(20) .map(this::toWebhookLogDto) .toList());
    }
    private UserContactDto toUserContactDto(User user) {
        Academy academy = user.getAcademy();
        return new UserContactDto(user.getId(), user.getNom(), user.getEmail(), user.getTel(), user.getMainRole() != null ? user.getMainRole() .name() : null, academy != null ? academy.getId() : null, academy != null ? academy.getName() : null, user.getActive());
    }
    private AcademyPaymentDto toPaymentDto(AcademyPayment payment) {
        Academy academy = payment.getAcademy();
        return new AcademyPaymentDto(payment.getId(), academy != null ? academy.getId() : null, academy != null ? academy.getName() : null, payment.getOffer() != null ? payment.getOffer() .name() : null, payment.getAmount(), payment.getCurrency(), payment.getStatus() != null ? payment.getStatus() .name() : null, payment.getPaymentMethod() != null ? payment.getPaymentMethod() .name() : null, payment.getReferenceCode(), payment.getNotes(), payment.getDueDate(), payment.getPaidAt(), payment.getCreatedAt());
    }
    private WebhookLogDto toWebhookLogDto(WebhookLog log) {
        return new WebhookLogDto(log.getId(), log.getWebhook() != null ? log.getWebhook() .getName() : null, log.getEventType(), log.getStatusCode(), log.getSuccess(), log.getErrorMessage(), log.getExecutedAt(), log.getResponseTimeMs());
    }
}
