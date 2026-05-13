package com.footballacademy.services.admin;

import com.footballacademy.model.Activity;
import com.footballacademy.model.Payment;
import com.footballacademy.model.Player;
import com.footballacademy.repository.ActivityRepository;
import com.footballacademy.repository.PaymentRepository;
import com.footballacademy.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.
class)
class AdvancedAdminDashboardServiceTest {
    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private ActivityRepository activityRepository;
    @InjectMocks
    private AdvancedAdminDashboardService dashboardService;
    private Player player1;
    private Player player2;
    private Payment payment1;
    private Payment payment2;
    private Activity activity1;
    private Activity activity2;
    @BeforeEach void setUp() {
        player1 = new Player();
        player1.setId(1L);
        player1.setGoals(10);
        player1.setAssists(5);
        player1.setAverageRating(7.5);
        player2 = new Player();
        player2.setId(2L);
        player2.setGoals(8);
        player2.setAssists(3);
        player2.setAverageRating(6.8);
        payment1 = new Payment();
        payment1.setId(1L);
        payment1.setAmount(100.0);
        payment1.setCurrency("USD");
        payment1.setPaid(true);
        payment1.setMois(LocalDate.now());
        payment2 = new Payment();
        payment2.setId(2L);
        payment2.setAmount(50.0);
        payment2.setCurrency("USD");
        payment2.setPaid(false);
        payment2.setMois(LocalDate.now());
        activity1 = new Activity();
        activity1.setId(1L);
        activity1.setTitle("Training Session");
        activity1.setType("TRAINING");
        activity1.setDate(LocalDateTime.now() .plusDays(1));
        activity2 = new Activity();
        activity2.setId(2L);
        activity2.setTitle("Match");
        activity2.setType("MATCH");
        activity2.setDate(LocalDateTime.now() .minusDays(1));
    }
    @Test void getDashboardKPIs_ShouldReturnComprehensiveKPIs() {
        when(playerRepository.count()) .thenReturn(2L);
        when(playerRepository.findAll()) .thenReturn(Arrays.asList(player1, player2));
        when(paymentRepository.findAll()) .thenReturn(Arrays.asList(payment1, payment2));
        when(activityRepository.findAll()) .thenReturn(Arrays.asList(activity1, activity2));
        Map<String, Object> kpis = dashboardService.getDashboardKPIs();
        assertNotNull(kpis);
        assertEquals(2L, kpis.get("totalPlayers"));
        assertEquals(2L, kpis.get("totalPayments"));
        assertEquals(1L, kpis.get("paidPayments"));
        assertEquals(1L, kpis.get("pendingPayments"));
        assertEquals(2L, kpis.get("totalActivities"));
        assertTrue((Double) kpis.get("totalRevenue") > 0);
        assertTrue((Double) kpis.get("pendingRevenue") > 0);
    }
    @Test void getDashboardKPIs_WhenNoData_ShouldReturnZeroValues() {
        when(playerRepository.count()) .thenReturn(0L);
        when(playerRepository.findAll()) .thenReturn(Arrays.asList());
        when(paymentRepository.findAll()) .thenReturn(Arrays.asList());
        when(activityRepository.findAll()) .thenReturn(Arrays.asList());
        Map<String, Object> kpis = dashboardService.getDashboardKPIs();
        assertNotNull(kpis);
        assertEquals(0L, kpis.get("totalPlayers"));
        assertEquals(0L, kpis.get("totalPayments"));
        assertEquals(0.0, kpis.get("totalRevenue"));
        assertEquals(0.0, kpis.get("pendingRevenue"));
    }
    @Test void getMonthlyRevenueTrend_ShouldReturnRevenueForSpecifiedMonths() {
        when(paymentRepository.findAll()) .thenReturn(Arrays.asList(payment1, payment2));
        Map<String, Object> trend = dashboardService.getMonthlyRevenueTrend(3);
        assertNotNull(trend);
        assertTrue(trend.size() <= 3);
        assertTrue(trend.values() .stream() .allMatch(value -> value instanceof Double));
    }
    @Test void getPlayerEngagementMetrics_ShouldReturnEngagementData() {
        when(playerRepository.findAll()) .thenReturn(Arrays.asList(player1, player2));
        Map<String, Object> metrics = dashboardService.getPlayerEngagementMetrics();
        assertNotNull(metrics);
        assertTrue(metrics.containsKey("playersByDivision"));
        assertTrue(metrics.containsKey("highPerformers"));
        assertTrue(metrics.containsKey("averagePerformers"));
        assertTrue(metrics.containsKey("lowPerformers"));
        assertTrue(metrics.containsKey("avgGoals"));
        assertTrue(metrics.containsKey("avgAssists"));
        assertTrue(metrics.containsKey("avgRating"));
    }
    @Test void getPlayerEngagementMetrics_WhenNoPlayers_ShouldReturnZeroValues() {
        when(playerRepository.findAll()) .thenReturn(Arrays.asList());
        Map<String, Object> metrics = dashboardService.getPlayerEngagementMetrics();
        assertNotNull(metrics);
        assertEquals(0L, metrics.get("highPerformers"));
        assertEquals(0L, metrics.get("averagePerformers"));
        assertEquals(0L, metrics.get("lowPerformers"));
        assertEquals(0.0, metrics.get("avgGoals"));
        assertEquals(0.0, metrics.get("avgAssists"));
        assertEquals(0.0, metrics.get("avgRating"));
    }
    @Test void getPaymentAgingReport_ShouldReturnAgingData() {
        Payment overduePayment = new Payment();
        overduePayment.setId(3L);
        overduePayment.setAmount(75.0);
        overduePayment.setPaid(false);
        overduePayment.setDueDate(LocalDateTime.now() .minusDays(15));
        when(paymentRepository.findByIsPaidFalse()) .thenReturn(Arrays.asList(payment2, overduePayment));
        Map<String, Object> report = dashboardService.getPaymentAgingReport();
        assertNotNull(report);
        assertTrue(report.containsKey("current0_30"));
        assertTrue(report.containsKey("overdue31_60"));
        assertTrue(report.containsKey("overdue61_90"));
        assertTrue(report.containsKey("overdue90Plus"));
    }
    @Test void getActivityParticipationReport_ShouldReturnActivityData() {
        when(activityRepository.findAll()) .thenReturn(Arrays.asList(activity1, activity2));
        Map<String, Object> report = dashboardService.getActivityParticipationReport();
        assertNotNull(report);
        assertTrue(report.containsKey("activitiesByType"));
        assertTrue(report.containsKey("upcomingActivities"));
        assertTrue(report.containsKey("pastActivities"));
        assertTrue(report.containsKey("activitiesThisWeek"));
    }
    @Test void getActivityParticipationReport_WhenNoActivities_ShouldReturnZeroValues() {
        when(activityRepository.findAll()) .thenReturn(Arrays.asList());
        Map<String, Object> report = dashboardService.getActivityParticipationReport();
        assertNotNull(report);
        assertEquals(0L, report.get("upcomingActivities"));
        assertEquals(0L, report.get("pastActivities"));
        assertEquals(0L, report.get("activitiesThisWeek"));
    }
    @Test void getDashboardKPIs_ShouldCalculateCorrectRates() {
        when(playerRepository.count()) .thenReturn(10L);
        when(playerRepository.findAll()) .thenReturn(Arrays.asList(player1, player2));
        when(paymentRepository.findAll()) .thenReturn(Arrays.asList(payment1, payment2));
        when(activityRepository.findAll()) .thenReturn(Arrays.asList(activity1, activity2));
        Map<String, Object> kpis = dashboardService.getDashboardKPIs();
        assertNotNull(kpis);
        // Check payment completion rate (1 paid out of 2 = 50%)
        double paymentCompletionRate =(Double) kpis.get("paymentCompletionRate");
        assertEquals(50.0, paymentCompletionRate, 0.01);
        // Check collection rate
        double collectionRate =(Double) kpis.get("collectionRate");
        assertEquals(50.0, collectionRate, 0.01);
        // Check financial health
        String financialHealth =(String) kpis.get("financialHealth");
        assertEquals("MODERATE", financialHealth);
    }
}
