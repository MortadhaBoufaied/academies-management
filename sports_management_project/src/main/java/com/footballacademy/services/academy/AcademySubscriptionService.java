package com.footballacademy.services.academy;

import com.footballacademy.model.Academy;
import com.footballacademy.model.AcademyPayment;
import com.footballacademy.repository.AcademyPaymentRepository;
import com.footballacademy.repository.AcademyRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service("academySubscriptionService")
@Transactional
public
class AcademySubscriptionService {
    public
    enum FeatureKey {
        USERS(false, "Users", "Manage academy accounts, players, trainers, and parents.", "/admin/view/users"), DIVISIONS(false, "Divisions", "Organize divisions and keep rosters structured.", "/admin/view/divisions"), ACTIVITIES(false, "Activities", "Plan trainings, matches, and academy events.", "/admin/view/activities"), ACADEMY_SETTINGS(false, "Academy Settings", "Keep academy identity and contact information up to date.", "/admin/view/academy-info"), SUBSCRIPTION(false, "Subscription", "Manage offer, invoices, and academy billing.", "/admin/view/subscription"), PAYMENTS(true, "Payments", "Track player fee collection and finance dashboards.", "/admin/view/payments"), NOTIFICATIONS(true, "Notifications", "Broadcast announcements to academy users.", "/admin/view/notifications"), REPORTS(true, "Reports", "Unlock analytics and operational reporting.", "/admin/view/reports"), CHATBOT(true, "Chatbot", "Maintain chatbot knowledge and academy assistance tools.", "/admin/view/chatbot"), CHAT(true, "Team Chat", "Use the academy communication and chat workspace.", "/admin/view/chat");
        private final boolean proOnly;
        private final String label;
        private final String description;
        private final String route;
        FeatureKey(boolean proOnly, String label, String description, String route) {
            this.proOnly = proOnly;
            this.label = label;
            this.description = description;
            this.route = route;
        }
        public boolean isProOnly() {
            return proOnly;
        }
        public String getLabel() {
            return label;
        }
        public String getDescription() {
            return description;
        }
        public String getRoute() {
            return route;
        }
        public static FeatureKey from(String value) {
            if (value == null || value.isBlank()) {
                return null;
            }
            try {
                return FeatureKey.valueOf(value.trim() .toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }
    }
    public
    record FeatureSummary(String key, String label, String description, String route, boolean proOnly, boolean available) {
    }
    private static final BigDecimal REGULAR_PRICE = BigDecimal.valueOf(149.00);
    private static final BigDecimal PRO_PRICE = BigDecimal.valueOf(349.00);
    private static final String CURRENCY = "TND";
    private final AcademyRepository academyRepository;
    private final AcademyPaymentRepository academyPaymentRepository;
    public AcademySubscriptionService(AcademyRepository academyRepository, AcademyPaymentRepository academyPaymentRepository) {
        this.academyRepository = academyRepository;
        this.academyPaymentRepository = academyPaymentRepository;
    }
    public BigDecimal priceFor(Academy.SubscriptionOffer offer) {
        if (offer == Academy.SubscriptionOffer.PRO) {
            return PRO_PRICE;
        } return REGULAR_PRICE;
    }
    public String currency() {
        return CURRENCY;
    }
    public List<FeatureSummary> featureCatalog(Academy academy) {
        return List.of(toSummary(academy, FeatureKey.USERS), toSummary(academy, FeatureKey.DIVISIONS), toSummary(academy, FeatureKey.ACTIVITIES), toSummary(academy, FeatureKey.ACADEMY_SETTINGS), toSummary(academy, FeatureKey.SUBSCRIPTION), toSummary(academy, FeatureKey.PAYMENTS), toSummary(academy, FeatureKey.NOTIFICATIONS), toSummary(academy, FeatureKey.REPORTS), toSummary(academy, FeatureKey.CHATBOT), toSummary(academy, FeatureKey.CHAT));
    }
    public Map<String, BigDecimal> offerPrices() {
        Map<String, BigDecimal> prices = new LinkedHashMap<>();
        prices.put(Academy.SubscriptionOffer.REGULAR.name(), REGULAR_PRICE);
        prices.put(Academy.SubscriptionOffer.PRO.name(), PRO_PRICE);
        return prices;
    }
    public FeatureSummary toSummary(Academy academy, FeatureKey key) {
        return new FeatureSummary(key.name(), key.getLabel(), key.getDescription(), key.getRoute(), key.isProOnly(), canUseFeature(academy, key));
    }
    public boolean canUseFeature(Academy academy, String featureKey) {
        return canUseFeature(academy, FeatureKey.from(featureKey));
    }
    public boolean canUseFeature(Academy academy, FeatureKey key) {
        if (key == null || academy == null) {
            return true;
        }
        if (key == FeatureKey.SUBSCRIPTION || key == FeatureKey.ACADEMY_SETTINGS) {
            return true;
        }
        if (!hasActiveSubscription(academy)) {
            return false;
        } return academy.getSubscriptionOffer() == Academy.SubscriptionOffer.PRO || !key.isProOnly();
    }
    public boolean hasActiveSubscription(Academy academy) {
        return academy != null && academy.getSubscriptionPaymentStatus() == Academy.SubscriptionPaymentStatus.PAID;
    }
    public boolean isBillingLocked(Academy academy) {
        return academy == null || !hasActiveSubscription(academy);
    }
    public String featureUrl(Academy academy, String featureKey, String defaultUrl) {
        if (canUseFeature(academy, featureKey)) {
            return defaultUrl;
        } return "/admin/view/subscription?locked=" + featureKey.toLowerCase(Locale.ROOT);
    }
    public String lockedReason(Academy academy, String featureKey) {
        if (canUseFeature(academy, featureKey)) {
            return "";
        }
        if (!hasActiveSubscription(academy)) {
            return "Complete the academy subscription payment to unlock this service.";
        } return "Upgrade to the Pro offer to unlock this service.";
    }
    public AcademyPayment createCheckout(Academy academy, Academy.SubscriptionOffer offer, String notes) {
        Academy persistedAcademy = academyRepository.findById(academy.getId()) .orElseThrow(() -> new IllegalArgumentException("Academy not found: " + academy.getId()));
        Academy.SubscriptionOffer desiredOffer = offer != null ? offer : Academy.SubscriptionOffer.REGULAR;
        if (hasActiveSubscription(persistedAcademy) && persistedAcademy.getSubscriptionOffer() == desiredOffer) {
            throw new IllegalArgumentException("This academy already has the " + desiredOffer.name() + " offer active");
        } Optional<AcademyPayment> existingPending = academyPaymentRepository .findTopByAcademy_IdAndStatusOrderByCreatedAtDesc(persistedAcademy.getId(), AcademyPayment.PaymentStatus.PENDING);
        if (existingPending.isPresent() && existingPending.get() .getOffer() == desiredOffer) {
            return existingPending.get();
        } AcademyPayment payment = new AcademyPayment();
        payment.setAcademy(persistedAcademy);
        payment.setOffer(desiredOffer);
        payment.setAmount(priceFor(desiredOffer));
        payment.setCurrency(CURRENCY);
        payment.setStatus(AcademyPayment.PaymentStatus.PENDING);
        payment.setPaymentMethod(AcademyPayment.PaymentMethod.MANUAL);
        payment.setDueDate(LocalDate.now() .plusDays(7));
        payment.setReferenceCode(buildReferenceCode(persistedAcademy, desiredOffer));
        payment.setNotes(notes != null && !notes.isBlank() ? notes : "Academy subscription checkout");
        return academyPaymentRepository.save(payment);
    }
    public AcademyPayment ensureInitialPayment(Academy academy) {
        if (academy == null || academy.getId() == null) {
            throw new IllegalArgumentException("Academy is required");
        } return createCheckout(academy, academy.getSubscriptionOffer(), "Initial academy subscription");
    }
    public AcademyPayment markPaymentPaid(Long paymentId, AcademyPayment.PaymentMethod paymentMethod, String notes) {
        AcademyPayment payment = academyPaymentRepository.findById(paymentId) .orElseThrow(() -> new IllegalArgumentException("Academy payment not found: " + paymentId));
        payment.markPaid(paymentMethod, notes);
        Academy academy = payment.getAcademy();
        academy.setSubscriptionOffer(payment.getOffer());
        academy.setSubscriptionPaymentStatus(Academy.SubscriptionPaymentStatus.PAID);
        if (academy.getSubscriptionActivatedAt() == null) {
            academy.setSubscriptionActivatedAt(LocalDateTime.now());
        } academy.setSubscriptionUpdatedAt(LocalDateTime.now());
        academyRepository.save(academy);
        return academyPaymentRepository.save(payment);
    }
    public AcademyPayment markPaymentPaidForAcademy(Long academyId, Long paymentId, AcademyPayment.PaymentMethod paymentMethod, String notes) {
        AcademyPayment payment = academyPaymentRepository.findById(paymentId) .orElseThrow(() -> new IllegalArgumentException("Academy payment not found: " + paymentId));
        if (payment.getAcademy() == null || !payment.getAcademy() .getId() .equals(academyId)) {
            throw new IllegalArgumentException("You cannot update another academy's invoice");
        } return markPaymentPaid(paymentId, paymentMethod, notes);
    }
    public void syncSubscriptionState(Academy academy, Academy.SubscriptionOffer offer, Academy.SubscriptionPaymentStatus paymentStatus, String notes) {
        Academy.SubscriptionOffer resolvedOffer = offer != null ? offer : Academy.SubscriptionOffer.REGULAR;
        Academy.SubscriptionPaymentStatus resolvedStatus = paymentStatus != null ? paymentStatus : Academy.SubscriptionPaymentStatus.PENDING;
        academy.setSubscriptionOffer(resolvedOffer);
        academy.setSubscriptionPaymentStatus(resolvedStatus);
        academy.setSubscriptionUpdatedAt(LocalDateTime.now());
        if (resolvedStatus == Academy.SubscriptionPaymentStatus.PAID && academy.getSubscriptionActivatedAt() == null) {
            academy.setSubscriptionActivatedAt(LocalDateTime.now());
        } academyRepository.save(academy);
        if (resolvedStatus == Academy.SubscriptionPaymentStatus.PAID) {
            Optional<AcademyPayment> pendingPayment = academyPaymentRepository .findTopByAcademy_IdAndStatusOrderByCreatedAtDesc(academy.getId(), AcademyPayment.PaymentStatus.PENDING);
            if (pendingPayment.isPresent() && pendingPayment.get() .getOffer() == resolvedOffer) {
                markPaymentPaid(pendingPayment.get() .getId(), AcademyPayment.PaymentMethod.MANUAL, defaultNotes(notes, "Manually approved by super admin"));
                return;
            } AcademyPayment payment = new AcademyPayment();
            payment.setAcademy(academy);
            payment.setOffer(resolvedOffer);
            payment.setAmount(priceFor(resolvedOffer));
            payment.setCurrency(CURRENCY);
            payment.setStatus(AcademyPayment.PaymentStatus.PAID);
            payment.setPaymentMethod(AcademyPayment.PaymentMethod.MANUAL);
            payment.setDueDate(LocalDate.now());
            payment.setReferenceCode(buildReferenceCode(academy, resolvedOffer));
            payment.setPaidAt(LocalDateTime.now());
            payment.setNotes(defaultNotes(notes, "Manual subscription activation"));
            academyPaymentRepository.save(payment);
            return;
        } createCheckout(academy, resolvedOffer, defaultNotes(notes, "Awaiting academy payment"));
    }
    public List<AcademyPayment> paymentsForAcademy(Long academyId) {
        return academyPaymentRepository.findByAcademy_IdOrderByCreatedAtDesc(academyId);
    }
    public List<AcademyPayment> allPayments() {
        return academyPaymentRepository.findAllByOrderByCreatedAtDesc();
    }
    public Optional<AcademyPayment> latestPayment(Long academyId) {
        return academyPaymentRepository.findTopByAcademy_IdOrderByCreatedAtDesc(academyId);
    }
    public Optional<AcademyPayment> latestPendingPayment(Long academyId) {
        return academyPaymentRepository.findTopByAcademy_IdAndStatusOrderByCreatedAtDesc(academyId, AcademyPayment.PaymentStatus.PENDING);
    }
    public Optional<AcademyPayment> findPayment(Long paymentId) {
        return academyPaymentRepository.findById(paymentId);
    }
    public long pendingPaymentsCount() {
        return academyPaymentRepository.countByStatus(AcademyPayment.PaymentStatus.PENDING);
    }
    public BigDecimal totalCollected() {
        return academyPaymentRepository.findAllByOrderByCreatedAtDesc() .stream() .filter(AcademyPayment::isPaid) .map(AcademyPayment::getAmount) .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    public FeatureKey featureForPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path == null) {
            return null;
        }
        if (path.startsWith("/admin/view/payments") || path.startsWith("/admin/payments")) {
            return FeatureKey.PAYMENTS;
        }
        if (path.startsWith("/admin/view/notifications") || path.startsWith("/admin/notifications")) {
            return FeatureKey.NOTIFICATIONS;
        }
        if (path.startsWith("/admin/view/reports")) {
            return FeatureKey.REPORTS;
        }
        if (path.startsWith("/admin/view/chatbot") || path.startsWith("/admin/chatbot") || path.startsWith("/admin/view/chatbot-data") || path.startsWith("/admin/view/bot-knowledge")) {
            return FeatureKey.CHATBOT;
        }
        if (path.startsWith("/admin/view/chat")) {
            return FeatureKey.CHAT;
        }
        if (path.startsWith("/admin/view/academy-info") || path.startsWith("/admin/academy-info")) {
            return FeatureKey.ACADEMY_SETTINGS;
        }
        if (path.startsWith("/admin/view/subscription") || path.startsWith("/admin/subscription")) {
            return FeatureKey.SUBSCRIPTION;
        }
        if (path.startsWith("/admin/view/divisions") || path.startsWith("/admin/divisions")) {
            return FeatureKey.DIVISIONS;
        }
        if (path.startsWith("/admin/view/activities") || path.startsWith("/admin/activities")) {
            return FeatureKey.ACTIVITIES;
        }
        if (path.startsWith("/admin/view/users") || path.startsWith("/admin/view/players") || path.startsWith("/admin/players") || path.startsWith("/admin/view/trainers") || path.startsWith("/admin/trainers") || path.startsWith("/admin/view/parents") || path.startsWith("/admin/parents")) {
            return FeatureKey.USERS;
        } return null;
    }
    public boolean canAccessPath(Academy academy, HttpServletRequest request) {
        FeatureKey featureKey = featureForPath(request);
        return featureKey == null || canUseFeature(academy, featureKey);
    }
    public String lockedRedirectPath(Academy academy, HttpServletRequest request) {
        FeatureKey featureKey = featureForPath(request);
        if (featureKey == null || canUseFeature(academy, featureKey)) {
            return null;
        } return "/admin/view/subscription?locked=" + featureKey.name() .toLowerCase(Locale.ROOT);
    }
    private String buildReferenceCode(Academy academy, Academy.SubscriptionOffer offer) {
        String slug = academy.getSlug() != null ? academy.getSlug() .toUpperCase(Locale.ROOT) .replace('-', '_') : "ACADEMY";
        return slug + "-" + offer.name() + "-" + LocalDate.now() .format(DateTimeFormatter.BASIC_ISO_DATE);
    }
    private String defaultNotes(String value, String fallback) {
        return value != null && !value.isBlank() ? value : fallback;
    }
}
