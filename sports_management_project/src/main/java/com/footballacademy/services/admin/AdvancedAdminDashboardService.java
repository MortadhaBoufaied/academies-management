package com.footballacademy.services.admin;

import com.footballacademy.config.AppUiProperties;
import com.footballacademy.model.Activity;
import com.footballacademy.model.Payment;
import com.footballacademy.model.Player;
import com.footballacademy.repository.ActivityRepository;
import com.footballacademy.repository.PaymentRepository;
import com.footballacademy.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public
class AdvancedAdminDashboardService {
    private static final Logger log = LoggerFactory.getLogger(AdvancedAdminDashboardService.
    class);
    private final PlayerRepository playerRepository;
    private final PaymentRepository paymentRepository;
    private final ActivityRepository activityRepository;
    private final AppUiProperties appUiProperties;
    /**      * Get comprehensive dashboard KPIs      */
    public Map<String, Object> getDashboardKPIs() {
        Map<String, Object> kpis = new HashMap<>();
        // Player statistics
        long totalPlayers = playerRepository.count();
        List<Player> allPlayers = playerRepository.findAll();
        kpis.put("totalPlayers", totalPlayers);
        // Payment statistics
        List<Payment> allPayments = paymentRepository.findAll();
        long totalPayments = allPayments.size();
        long paidPayments = allPayments.stream() .filter(Payment::isPaid) .count();
        long pendingPayments = totalPayments - paidPayments;
        double totalRevenue = allPayments.stream() .filter(Payment::isPaid) .mapToDouble(Payment::getAmount) .sum();
        double pendingRevenue = allPayments.stream() .filter(payment -> !payment.isPaid()) .mapToDouble(Payment::getAmount) .sum();
        kpis.put("totalPayments", totalPayments);
        kpis.put("paidPayments", paidPayments);
        kpis.put("pendingPayments", pendingPayments);
        kpis.put("totalRevenue", totalRevenue);
        kpis.put("pendingRevenue", pendingRevenue);
        // Activity statistics
        List<Activity> allActivities = activityRepository.findAll();
        long totalActivities = allActivities.size();
        long upcomingActivities = allActivities.stream() .filter(activity -> activity.getDate() .isAfter(LocalDate.now())) .count();
        long pastActivities = totalActivities - upcomingActivities;
        kpis.put("totalActivities", totalActivities);
        kpis.put("upcomingActivities", upcomingActivities);
        kpis.put("pastActivities", pastActivities);
        // Calculate rates
        double paymentCompletionRate = totalPayments > 0 ?(paidPayments * 100.0 / totalPayments) : 0.0;
        double collectionRate = totalRevenue > 0 ?(totalRevenue /(totalRevenue + pendingRevenue)) * 100.0 : 0.0;
        kpis.put("paymentCompletionRate", paymentCompletionRate);
        kpis.put("collectionRate", collectionRate);
        // Financial health assessment
        String financialHealth = assessFinancialHealth(paymentCompletionRate, collectionRate);
        kpis.put("financialHealth", financialHealth);
        // Player engagement
        double avgGoals = allPlayers.stream() .mapToDouble(Player::getGoals) .average() .orElse(0.0);
        double avgAssists = allPlayers.stream() .mapToDouble(Player::getAssists) .average() .orElse(0.0);
        double avgRating = allPlayers.stream() .mapToDouble(Player::getAverageRating) .average() .orElse(0.0);
        kpis.put("avgGoals", avgGoals);
        kpis.put("avgAssists", avgAssists);
        kpis.put("avgRating", avgRating);
        log.info("Dashboard KPIs calculated: {} players, {} payments, {} activities", totalPlayers, totalPayments, totalActivities);
        return kpis;
    }
    /**      * Get monthly revenue trend for specified number of months      */
    public Map<String, Object> getMonthlyRevenueTrend(int months) {
        Map<String, Object> trend = new LinkedHashMap<>();
        LocalDate endDate = LocalDate.now();
        for (int i = months - 1;
        i >= 0;
        i--) {
            YearMonth yearMonth = YearMonth.from(endDate.minusMonths(i));
            LocalDate startOfMonth = yearMonth.atDay(1);
            LocalDate endOfMonth = yearMonth.atEndOfMonth();
            List<Payment> monthPayments = paymentRepository.findAll() .stream() .filter(payment -> payment.getMois() != null) .filter(payment -> !payment.getMois() .isBefore(startOfMonth) && !payment.getMois() .isAfter(endOfMonth)) .filter(Payment::isPaid) .collect(Collectors.toList());
            double monthlyRevenue = monthPayments.stream() .mapToDouble(Payment::getAmount) .sum();
            String monthKey = yearMonth.getMonth() .name() + " " + yearMonth.getYear();
            trend.put(monthKey, monthlyRevenue);
        } log.info("Monthly revenue trend calculated for {} months", months);
        return trend;
    }
    /**      * Get player engagement metrics      */
    public Map<String, Object> getPlayerEngagementMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        List<Player> allPlayers = playerRepository.findAll();
        // Categorize players by performance
        long highPerformers = allPlayers.stream() .filter(player -> player.getAverageRating() >= 7.5) .count();
        long averagePerformers = allPlayers.stream() .filter(player -> player.getAverageRating() >= 6.0 && player.getAverageRating() < 7.5) .count();
        long lowPerformers = allPlayers.stream() .filter(player -> player.getAverageRating() < 6.0) .count();
        metrics.put("highPerformers", highPerformers);
        metrics.put("averagePerformers", averagePerformers);
        metrics.put("lowPerformers", lowPerformers);
        // Average statistics
        double avgGoals = allPlayers.stream() .mapToDouble(Player::getGoals) .average() .orElse(0.0);
        double avgAssists = allPlayers.stream() .mapToDouble(Player::getAssists) .average() .orElse(0.0);
        double avgRating = allPlayers.stream() .mapToDouble(Player::getAverageRating) .average() .orElse(0.0);
        metrics.put("avgGoals", avgGoals);
        metrics.put("avgAssists", avgAssists);
        metrics.put("avgRating", avgRating);
        // Players by division (if division info is available)
        Map<String, Long> playersByDivision = allPlayers.stream() .filter(player -> player.getDivision() != null) .collect(Collectors.groupingBy(player -> player.getDivision() .getNom(), Collectors.counting()));
        metrics.put("playersByDivision", playersByDivision);
        // Top performers
        List<Player> topPerformers = allPlayers.stream() .sorted((p1, p2) -> Double.compare(p2.getAverageRating(), p1.getAverageRating())) .limit(appUiProperties.getDashboard() .getRecentItemsLimit()) .collect(Collectors.toList());
        metrics.put("topPerformers", topPerformers);
        log.info("Player engagement metrics calculated: {} high, {} average, {} low performers", highPerformers, averagePerformers, lowPerformers);
        return metrics;
    }
    /**      * Get payment aging report      */
    public Map<String, Object> getPaymentAgingReport() {
        Map<String, Object> report = new HashMap<>();
        LocalDate now = LocalDate.now();
        List<Payment> unpaidPayments = paymentRepository.findByIsPaidFalse();
        // Categorize by aging
        double current0_30 = unpaidPayments.stream() .filter(payment -> payment.getDueDate() != null) .filter(payment -> payment.getDueDate() .isAfter(now.minusDays(30))) .mapToDouble(Payment::getAmount) .sum();
        double overdue31_60 = unpaidPayments.stream() .filter(payment -> payment.getDueDate() != null) .filter(payment -> {
            LocalDate dueDate = payment.getDueDate();
            return dueDate.isBefore(now.minusDays(30)) && dueDate.isAfter(now.minusDays(60));
        }) .mapToDouble(Payment::getAmount) .sum();
        double overdue61_90 = unpaidPayments.stream() .filter(payment -> payment.getDueDate() != null) .filter(payment -> {
            LocalDate dueDate = payment.getDueDate();
            return dueDate.isBefore(now.minusDays(60)) && dueDate.isAfter(now.minusDays(90));
        }) .mapToDouble(Payment::getAmount) .sum();
        double overdue90Plus = unpaidPayments.stream() .filter(payment -> payment.getDueDate() != null) .filter(payment -> payment.getDueDate() .isBefore(now.minusDays(90))) .mapToDouble(Payment::getAmount) .sum();
        report.put("current0_30", current0_30);
        report.put("overdue31_60", overdue31_60);
        report.put("overdue61_90", overdue61_90);
        report.put("overdue90Plus", overdue90Plus);
        // Count by aging
        long count0_30 = unpaidPayments.stream() .filter(payment -> payment.getDueDate() != null) .filter(payment -> payment.getDueDate() .isAfter(now.minusDays(30))) .count();
        long count31_60 = unpaidPayments.stream() .filter(payment -> payment.getDueDate() != null) .filter(payment -> {
            LocalDate dueDate = payment.getDueDate();
            return dueDate.isBefore(now.minusDays(30)) && dueDate.isAfter(now.minusDays(60));
        }) .count();
        long count61_90 = unpaidPayments.stream() .filter(payment -> payment.getDueDate() != null) .filter(payment -> {
            LocalDate dueDate = payment.getDueDate();
            return dueDate.isBefore(now.minusDays(60)) && dueDate.isAfter(now.minusDays(90));
        }) .count();
        long count90Plus = unpaidPayments.stream() .filter(payment -> payment.getDueDate() != null) .filter(payment -> payment.getDueDate() .isBefore(now.minusDays(90))) .count();
        report.put("count0_30", count0_30);
        report.put("count31_60", count31_60);
        report.put("count61_90", count61_90);
        report.put("count90Plus", count90Plus);
        double totalOverdue = overdue31_60 + overdue61_90 + overdue90Plus;
        report.put("totalOverdue", totalOverdue);
        log.info("Payment aging report: current={}, overdue31_60={}, overdue61_90={}, overdue90Plus={}", current0_30, overdue31_60, overdue61_90, overdue90Plus);
        return report;
    }
    /**      * Get activity participation report      */
    public Map<String, Object> getActivityParticipationReport() {
        Map<String, Object> report = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekStart = now.toLocalDate() .atStartOfDay();
        LocalDateTime weekEnd = now.toLocalDate() .plusDays(7) .atTime(23, 59, 59);
        List<Activity> allActivities = activityRepository.findAll();
        // Activities by type
        Map<String, Long> activitiesByType = allActivities.stream() .collect(Collectors.groupingBy(Activity::getType, Collectors.counting()));
        report.put("activitiesByType", activitiesByType);
        // Upcoming vs past activities
        long upcomingActivities = allActivities.stream() .filter(activity -> activity.getDate() .isAfter(now.toLocalDate())) .count();
        long pastActivities = allActivities.stream() .filter(activity -> activity.getDate() .isBefore(now.toLocalDate())) .count();
        report.put("upcomingActivities", upcomingActivities);
        report.put("pastActivities", pastActivities);
        // Activities this week
        LocalDate weekStartDate = weekStart.toLocalDate();
        LocalDate weekEndDate = weekEnd.toLocalDate();
        long activitiesThisWeek = allActivities.stream() .filter(activity -> !activity.getDate() .isBefore(weekStartDate) && !activity.getDate() .isAfter(weekEndDate)) .count();
        report.put("activitiesThisWeek", activitiesThisWeek);
        // Activities by month (current month)
        YearMonth currentMonth = YearMonth.from(now);
        LocalDate monthStart = currentMonth.atDay(1);
        LocalDate monthEnd = currentMonth.atEndOfMonth();
        long activitiesThisMonth = allActivities.stream() .filter(activity -> {
            LocalDate activityDate = activity.getDate();
            return !activityDate.isBefore(monthStart) && !activityDate.isAfter(monthEnd);
        }) .count();
        report.put("activitiesThisMonth", activitiesThisMonth);
        // Most recent activities
        List<Activity> recentActivities = allActivities.stream() .filter(activity -> activity.getDate() .isBefore(now.toLocalDate())) .sorted((a1, a2) -> a2.getDate() .compareTo(a1.getDate())) .limit(appUiProperties.getDashboard() .getRecentItemsLimit()) .collect(Collectors.toList());
        report.put("recentActivities", recentActivities);
        // Upcoming activities
        List<Activity> upcomingList = allActivities.stream() .filter(activity -> activity.getDate() .isAfter(now.toLocalDate())) .sorted(Comparator.comparing(Activity::getDate)) .limit(appUiProperties.getDashboard() .getRecentItemsLimit()) .collect(Collectors.toList());
        report.put("upcomingActivitiesList", upcomingList);
        log.info("Activity participation report: total={}, upcoming={}, past={}, thisWeek={}", allActivities.size(), upcomingActivities, pastActivities, activitiesThisWeek);
        return report;
    }
    /**      * Get complete dashboard overview      */
    public Map<String, Object> getDashboardOverview() {
        Map<String, Object> overview = new HashMap<>();
        overview.put("kpis", getDashboardKPIs());
        overview.put("revenueTrend", getMonthlyRevenueTrend(appUiProperties.getDashboard() .getChartMonths()));
        overview.put("playerEngagement", getPlayerEngagementMetrics());
        overview.put("paymentAging", getPaymentAgingReport());
        overview.put("activityParticipation", getActivityParticipationReport());
        // Add timestamp
        overview.put("generatedAt", LocalDateTime.now());
        log.info("Complete dashboard overview generated");
        return overview;
    }
    /**      * Assess financial health based on payment metrics      */
    private String assessFinancialHealth(double paymentCompletionRate, double collectionRate) {
        if (paymentCompletionRate >= 90 && collectionRate >= 90) {
            return "EXCELLENT";
        } else
        if (paymentCompletionRate >= 75 && collectionRate >= 75) {
            return "GOOD";
        } else
        if (paymentCompletionRate >= 50 && collectionRate >= 50) {
            return "MODERATE";
        } else {
            return "POOR";
        }
    }
}
