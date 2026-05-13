package com.footballacademy.services;

import com.footballacademy.DTO.PaymentDto;
import com.footballacademy.config.AppUiProperties;
import com.footballacademy.exception.*;
import com.footballacademy.model.*;
import com.footballacademy.repository.ParentRepository;
import com.footballacademy.repository.PaymentRepository;
import com.footballacademy.repository.PlayerRepository;
import com.footballacademy.services.academy.AcademyAccessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class PaymentService {

    private static final Logger logger =
            LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final ParentRepository parentRepository;
    private final PlayerRepository playerRepository;
    private final NotificationService notificationService;
    private final AcademyAccessService academyAccessService;
    private final AppUiProperties appUiProperties;

    public PaymentService(
            PaymentRepository paymentRepository,
            ParentRepository parentRepository,
            PlayerRepository playerRepository,
            NotificationService notificationService,
            AcademyAccessService academyAccessService,
            AppUiProperties appUiProperties
    ) {
        this.paymentRepository = paymentRepository;
        this.parentRepository = parentRepository;
        this.playerRepository = playerRepository;
        this.notificationService = notificationService;
        this.academyAccessService = academyAccessService;
        this.appUiProperties = appUiProperties;
    }

    // =====================================================
    // === QUERIES
    // =====================================================

    public List<PaymentDto> getUnpaidPaymentsForCurrentMonth() {
        LocalDate now = LocalDate.now();
        return visiblePayments(
                paymentRepository.findUnpaidByMonth(
                        now.getYear(),
                        now.getMonthValue()
                )
        ).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<PaymentDto> getPaymentsForMonth(int year, int month) {
        validateYearMonth(year, month);
        LocalDate date = LocalDate.of(year, month, 1);
        return visiblePayments(paymentRepository.findByMois(date))
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<PaymentDto> getParentPaymentsForMonth(Long parentId) {
        if (parentId == null) {
            throw new ValidationException("parentId", "Parent ID cannot be null");
        }

        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent", parentId));

        assertParentVisible(parent);

        LocalDate month = LocalDate.now().withDayOfMonth(1);

        return visiblePayments(
                paymentRepository.findByParentIdAndMois(parentId, month)
        ).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // =====================================================
    // === PAYMENT UPDATES (SAFE)
    // =====================================================

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Payment updatePaymentStatus(Long paymentId, boolean paid) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> PaymentException.paymentNotFound(paymentId));

        assertPaymentVisible(payment);

        if (payment.isPaid() == paid) {
            return payment;
        }

        payment.setPaid(paid);
        payment.setStatus(paid ? "PAID" : "PENDING");

        try {
            return paymentRepository.save(payment);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new BusinessException(
                    "PAYMENT_CONCURRENT_MODIFICATION",
                    "This payment is being modified by another user"
            );
        }
    }

    // =====================================================
    // === CREATION
    // =====================================================

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Payment createPayment(Long playerId, Double amount, LocalDate month) {

        if (playerId == null) {
            throw new ValidationException("playerId", "Player ID cannot be null");
        }

        if (amount == null || amount <= 0) {
            throw PaymentException.invalidAmount(amount);
        }

        if (month == null || month.isAfter(LocalDate.now())) {
            throw new ValidationException("month", "Invalid payment month");
        }

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player", playerId));

        assertPlayerVisible(player);

        if (player.getParent() == null) {
            throw new ValidationException("parent", "Player has no parent");
        }

        Parent parent = player.getParent();
        assertParentVisible(parent);

        boolean exists = paymentRepository.findByMois(month).stream()
                .anyMatch(p -> playerId.equals(p.getPlayerId()));

        if (exists) {
            throw new BusinessException("PAYMENT_EXISTS", "Payment already exists");
        }

        Payment payment = new Payment(amount, month, player, parent);
        payment.setAcademy(player.getAcademy());
        payment.setDueDate(month.withDayOfMonth(1));
        payment.setDescription("Monthly fee for " + month);

        try {
            return paymentRepository.save(payment);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException("PAYMENT_EXISTS", "Duplicate payment detected");
        }
    }

    // =====================================================
    // === NOTIFICATIONS
    // =====================================================

    @Transactional
    public void sendPaymentReminderToParents(Long adminId) {
        LocalDate now = LocalDate.now();
        List<Payment> unpaid =
                visiblePayments(paymentRepository.findUnpaidByMonth(
                        now.getYear(), now.getMonthValue()
                ));

        Map<Long, List<Payment>> byParent =
                unpaid.stream().collect(Collectors.groupingBy(Payment::getParentId));

        for (Map.Entry<Long, List<Payment>> entry : byParent.entrySet()) {
            Long parentId = entry.getKey();
            List<Payment> payments = entry.getValue();

            Parent parent = parentRepository.findById(parentId).orElse(null);
            if (parent == null) continue;

            double total = payments.stream()
                    .mapToDouble(Payment::getMontant)
                    .sum();

            Notification n = new Notification(
                    "Payment Reminder",
                    "You have unpaid payments totaling " + total,
                    Notification.Category.PARENTS,
                    parent.getUser().getId(),
                    adminId
            );

            notificationService.saveNotification(n);
        }
    }

    // =====================================================
    // === DTO HELPERS
    // =====================================================

    private PaymentDto toDto(Payment p) {
        PaymentDto dto = new PaymentDto(
                p.getId(),
                p.getMontant(),
                p.getMois(),
                p.isPaid(),
                p.getPlayerId(),
                p.getParentId()
        );

        dto.setStatus(p.getStatus());
        dto.setDueDate(p.getDueDate());
        dto.setCurrency(p.getCurrency());
        dto.setDescription(p.getDescription());

        return dto;
    }

    // =====================================================
    // === VISIBILITY & SECURITY
    // =====================================================

    private List<Payment> visiblePayments(List<Payment> payments) {
        if (academyAccessService.isSuperAdmin()) return payments;
        Long academyId = academyAccessService.currentAcademyId();

        return payments.stream()
                .filter(p -> p.getAcademy() != null
                        && Objects.equals(p.getAcademy().getId(), academyId))
                .collect(Collectors.toList());
    }

    private void assertPaymentVisible(Payment payment) {
        if (!visiblePayments(List.of(payment)).contains(payment)) {
            throw new AccessDeniedException("Payment not accessible");
        }
    }

    private void assertPlayerVisible(Player p) {
        if (!academyAccessService.isSuperAdmin()
                && !academyAccessService.canAccessAcademy(p.getAcademy())) {
            throw new AccessDeniedException("Player not accessible");
        }
    }

    private void assertParentVisible(Parent p) {
        if (!academyAccessService.isSuperAdmin()
                && !academyAccessService.canAccessAcademy(p.getAcademy())) {
            throw new AccessDeniedException("Parent not accessible");
        }
    }

    private void validateYearMonth(int year, int month) {
        int min = appUiProperties.getPayments().getMinAllowedYear();
        int max = appUiProperties.getPayments().getMaxAllowedYear();

        if (year < min || year > max) {
            throw new ValidationException("year", "Invalid year");
        }
        if (month < 1 || month > 12) {
            throw new ValidationException("month", "Invalid month");
        }
    }

    // ==================== MISSING METHODS ====================
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public List<Payment> getPaymentsForPlayer(Long playerId) {
        Player player = playerRepository.findById(playerId).orElseThrow(() -> new RuntimeException("Player not found"));
        assertPlayerVisible(player);
        return paymentRepository.findByPlayerId(playerId);
    }

    public List<Payment> getPaymentsForParent(Long parentId) {
        Parent parent = parentRepository.findById(parentId).orElseThrow(() -> new RuntimeException("Parent not found"));
        assertParentVisible(parent);
        return paymentRepository.findAll().stream()
                .filter(p -> p.getPlayerId() != null)
                .filter(p -> {
                    Player player = playerRepository.findById(p.getPlayerId()).orElse(null);
                    return player != null && parent.getId().equals(player.getParent().getId());
                })
                .collect(Collectors.toList());
    }

    public Payment markAsPaid(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() -> new RuntimeException("Payment not found"));
        return updatePaymentStatus(paymentId, true);
    }

    public void deletePayment(Long paymentId) {
        if (!paymentRepository.existsById(paymentId)) {
            throw new RuntimeException("Payment not found");
        }
        paymentRepository.deleteById(paymentId);
    }

    public List<Payment> getOverduePayments() {
        List<Payment> payments = paymentRepository.findAll();
        return payments.stream()
                .filter(p -> !p.isPaid())
                .collect(Collectors.toList());
    }

    public List<Payment> getPendingPayments() {
        List<Payment> payments = paymentRepository.findAll();
        return payments.stream()
                .filter(p -> !p.isPaid())
                .collect(Collectors.toList());
    }

    public Double getMonthlyRevenue() {
        List<Payment> payments = paymentRepository.findAll();
        return payments.stream()
                .filter(Payment::isPaid)
                .mapToDouble(Payment::getAmount)
                .sum();
    }

    public void resetMonthlyPayments() {
        List<Payment> payments = paymentRepository.findAll();
        payments.forEach(p -> p.setPaid(false));
        paymentRepository.saveAll(payments);
    }

    public void createMonthlyPaymentsForAllPlayers(LocalDate month, double amount) {
        playerRepository.findAll().forEach(player -> {
            createPayment(player.getId(), amount, month);
        });
    }

    public List<Parent> getParentsWhoDidntPayThisMonth() {
        List<Parent> parentsWhoDidntPay = new ArrayList<>();
        List<Payment> unpaidPayments = paymentRepository.findAll().stream()
                .filter(p -> !p.isPaid())
                .collect(Collectors.toList());
        
        for (Payment payment : unpaidPayments) {
            Player player = playerRepository.findById(payment.getPlayerId()).orElse(null);
            if (player != null && player.getParent() != null) {
                parentsWhoDidntPay.add(player.getParent());
            }
        }
        return parentsWhoDidntPay.stream().distinct().collect(Collectors.toList());
    }
}
