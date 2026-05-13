package com.footballacademy.controllers_rest.superadmin;

import com.footballacademy.model.Academy;
import com.footballacademy.model.AcademyPayment;
import com.footballacademy.model.User;
import com.footballacademy.repository.*;
import com.footballacademy.services.scouting.AcademyPerformanceRankingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/super-admin/dashboard")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SuperAdminDashboardSummaryController {
    private final AcademyRepository academyRepository;
    private final SportRepository sportRepository;
    private final UserRepository userRepository;
    private final AcademyPaymentRepository academyPaymentRepository;
    private final NotificationRepository notificationRepository;
    private final AcademyPerformanceRankingService rankingService;

    public SuperAdminDashboardSummaryController(
            AcademyRepository academyRepository,
            SportRepository sportRepository,
            UserRepository userRepository,
            AcademyPaymentRepository academyPaymentRepository,
            NotificationRepository notificationRepository,
            AcademyPerformanceRankingService rankingService
    ) {
        this.academyRepository = academyRepository;
        this.sportRepository = sportRepository;
        this.userRepository = userRepository;
        this.academyPaymentRepository = academyPaymentRepository;
        this.notificationRepository = notificationRepository;
        this.rankingService = rankingService;
    }

    @GetMapping("/summary")
    public ResponseEntity<?> summary() {
        long unreadSystemAlerts = notificationRepository.findAll().stream()
                .filter(notification -> !notification.isRead())
                .count();

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalAcademies", academyRepository.count());
        summary.put("activeAcademies", academyRepository.countByStatus(Academy.AcademyStatus.ACTIVE));
        summary.put("suspendedAcademies", academyRepository.countByStatus(Academy.AcademyStatus.SUSPENDED));
        summary.put("totalSports", sportRepository.count());
        summary.put("totalUsers", userRepository.count());
        summary.put("totalAdmins", userRepository.countByMainRole(User.UserRole.ADMIN));
        summary.put("totalPlayers", userRepository.countByMainRole(User.UserRole.PLAYER));
        summary.put("totalTrainers", userRepository.countByMainRole(User.UserRole.TRAINER));
        summary.put("totalParents", userRepository.countByMainRole(User.UserRole.PARENT));
        summary.put("totalScouters", userRepository.countByMainRole(User.UserRole.SCOUTER));
        summary.put("pendingPayments", academyPaymentRepository.countByStatus(AcademyPayment.PaymentStatus.PENDING));
        summary.put("completedPayments", academyPaymentRepository.countByStatus(AcademyPayment.PaymentStatus.PAID));
        summary.put("unreadSystemAlerts", unreadSystemAlerts);
        summary.put("activeSubscriptions", academyRepository.findAll().stream().filter(a -> a.getSubscriptionPaymentStatus() == Academy.SubscriptionPaymentStatus.PAID).count());
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/rankings/recompute")
    public ResponseEntity<?> recomputeRankings() {
        return ResponseEntity.ok(Map.of("items", rankingService.recomputeAllAcademyScores(), "message", "Academy rankings recomputed"));
    }
}
