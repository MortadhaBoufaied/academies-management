package com.footballacademy.controllers_mvc;

import com.footballacademy.config.AppUiProperties;
import com.footballacademy.model.Activity;
import com.footballacademy.model.Academy;
import com.footballacademy.model.Payment;
import com.footballacademy.repository.NotificationRepository;
import com.footballacademy.services.PaymentService;
import com.footballacademy.services.academy.AcademyAccessService;
import com.footballacademy.services.academy.AcademySubscriptionService;
import com.footballacademy.services.activity.ActivityService;
import com.footballacademy.services.admin.AdminService;
import com.footballacademy.services.dashboard.DashboardTopPlayersService;
import com.footballacademy.services.division.DivisionService;
import com.footballacademy.services.player.PlayerService;
import com.footballacademy.services.trainer.TrainerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/admin/view")
public
class DashboardViewController {
    private final PlayerService playerService;
    private final TrainerService trainerService;
    private final DivisionService divisionService;
    private final ActivityService activityService;
    private final PaymentService paymentService;
    private final NotificationRepository notificationRepository;
    private final AdminService adminService;
    private final AcademyAccessService academyAccessService;
    private final AcademySubscriptionService academySubscriptionService;
    private final AppUiProperties appUiProperties;
    private final DashboardTopPlayersService dashboardTopPlayersService;
    public DashboardViewController(PlayerService playerService, TrainerService trainerService, DivisionService divisionService, ActivityService activityService, PaymentService paymentService, NotificationRepository notificationRepository, AdminService adminService, AcademyAccessService academyAccessService, AcademySubscriptionService academySubscriptionService, AppUiProperties appUiProperties, DashboardTopPlayersService dashboardTopPlayersService) {
        this.playerService = playerService;
        this.trainerService = trainerService;
        this.divisionService = divisionService;
        this.activityService = activityService;
        this.paymentService = paymentService;
        this.notificationRepository = notificationRepository;
        this.adminService = adminService;
        this.academyAccessService = academyAccessService;
        this.academySubscriptionService = academySubscriptionService;
        this.appUiProperties = appUiProperties;
        this.dashboardTopPlayersService = dashboardTopPlayersService;
    }
    @GetMapping({
        "", "/", "/dashboard"
    })
    public String dashboard(Model model) {
        Academy academy = academyAccessService.currentAcademyOrThrow();
        List<Activity> activities = activityService.getAllActivities();
        List<Payment> payments = paymentService.getAllPayments();
        long paidPayments = payments.stream() .filter(Payment::isPaid) .count();
        long pendingPayments = payments.size() - paidPayments;
        String paymentRate = payments.isEmpty() ? "0" : String.format("%.1f", paidPayments * 100.0 / payments.size());
        List<String> chartMonths = new ArrayList<>();
        List<Double> chartRevenue = new ArrayList<>();
        List<Long> chartPendingPayments = new ArrayList<>();
        List<Long> chartActivityCounts = new ArrayList<>();
        LocalDate now = LocalDate.now() .withDayOfMonth(1);
        int chartMonthWindow = Math.max(1, appUiProperties.getDashboard() .getChartMonths());
        for (int i = chartMonthWindow - 1;
        i >= 0;
        i--) {
            LocalDate month = now.minusMonths(i);
            String label = month.getMonth() .getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + month.getYear();
            chartMonths.add(label);
            double monthRevenue = payments.stream() .filter(Payment::isPaid) .filter(p -> p.getMois() != null && p.getMois() .getYear() == month.getYear() && p.getMois() .getMonthValue() == month.getMonthValue()) .map(Payment::getMontant) .filter(v -> v != null) .mapToDouble(Double::doubleValue) .sum();
            chartRevenue.add(monthRevenue);
            long monthPending = payments.stream() .filter(p -> !p.isPaid()) .filter(p -> p.getMois() != null && p.getMois() .getYear() == month.getYear() && p.getMois() .getMonthValue() == month.getMonthValue()) .count();
            chartPendingPayments.add(monthPending);
            long monthActivities = activities.stream() .filter(a -> a.getDate() != null && a.getDate() .getYear() == month.getYear() && a.getDate() .getMonthValue() == month.getMonthValue()) .count();
            chartActivityCounts.add(monthActivities);
        } model.addAttribute("totalPlayers", playerService.getAllPlayers() .size());
        model.addAttribute("totalTrainers", trainerService.getAllTrainersCombined() .size());
        model.addAttribute("totalDivisions", divisionService.getAllDivisions() .size());
        model.addAttribute("totalActivities", activities.size());
        model.addAttribute("pendingPayments", pendingPayments);
        model.addAttribute("paymentRate", paymentRate);
        model.addAttribute("totalUsers", adminService.getAllUsers() .size());
        model.addAttribute("unreadCount", unreadCount());
        model.addAttribute("academySubscriptionLocked", academySubscriptionService.isBillingLocked(academy));
        model.addAttribute("academyFeatureCatalog", academySubscriptionService.featureCatalog(academy));
        model.addAttribute("recentActivities", activities.stream() .filter(activity -> activity.getDate() != null) .sorted(Comparator.comparing(Activity::getDate) .reversed()) .limit(appUiProperties.getDashboard() .getRecentItemsLimit()) .toList());
        model.addAttribute("chartMonths", chartMonths);
        model.addAttribute("chartRevenue", chartRevenue);
        model.addAttribute("chartPendingPayments", chartPendingPayments);
        model.addAttribute("chartActivityCounts", chartActivityCounts);
        model.addAttribute("topPlayers", dashboardTopPlayersService.topPlayersForCurrentAcademy(5));
        model.addAttribute("adminServiceLinks", List.of(buildLink(academy, "DIVISIONS", "Divisions", "account_tree", "/admin/view/divisions", "Organize groups by sport category and level."), buildLink(academy, "ACTIVITIES", "Activities", "calendar_month", "/admin/view/activities", "Plan trainings, matches, and events."), buildLink(academy, "REPORTS", "Statistics", "bar_chart", "/admin/view/reports", "Operational and financial analytics."), buildLink(academy, "USERS", "Manage Users", "manage_accounts", "/admin/view/users", "Create, edit, search, and remove academy accounts."), buildLink(academy, "NOTIFICATIONS", "Send Notification", "campaign", "/admin/view/notifications", "Broadcast messages to academy users."), buildLink(academy, "CHATBOT", "Chatbot Knowledge", "smart_toy", "/admin/view/bot-knowledge", "Maintain academy chatbot answers and files.")));
        return "pages/modules/admin/dashboard";
    }
    @GetMapping({
        "/home", "/home/index"
    })
    public String home(Model model) {
        model.addAttribute("topPlayers", dashboardTopPlayersService.topPlayersForCurrentAcademy(5));
        return "pages/modules/home/index";
    }
    private long unreadCount() {
        Long userId = academyAccessService.currentUserId();
        if (userId != null) {
            return notificationRepository.countByUserIdAndIsReadFalse(userId);
        }
        if (academyAccessService.isSuperAdmin()) {
            return notificationRepository.count();
        } Long academyId = academyAccessService.currentAcademyId();
        return academyId != null ? notificationRepository.findByAcademy_Id(academyId) .size() : 0;
    }
    private AdminServiceLink buildLink(Academy academy, String featureKey, String label, String icon, String href, String description) {
        return new AdminServiceLink(label, icon, academySubscriptionService.featureUrl(academy, featureKey, href), description, academySubscriptionService.canUseFeature(academy, featureKey));
    }
    public
    record AdminServiceLink(String label, String icon, String href, String description, boolean available) {
    }
}
