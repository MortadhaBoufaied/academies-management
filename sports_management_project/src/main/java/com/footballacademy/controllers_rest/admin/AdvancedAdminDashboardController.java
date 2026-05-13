package com.footballacademy.controllers_rest.admin;

import com.footballacademy.services.admin.AdvancedAdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard/advanced")
@RequiredArgsConstructor
public
class AdvancedAdminDashboardController {
    private final AdvancedAdminDashboardService advancedAdminDashboardService;
    /**      * Get comprehensive dashboard KPIs      */
    @GetMapping("/kpis")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getDashboardKPIs() {
        try {
            Map<String, Object> kpis = advancedAdminDashboardService.getDashboardKPIs();
            return ResponseEntity.ok(kpis);
        } catch (Exception e) {
            return ResponseEntity.status(500) .body(Map.of("error", "Failed to get dashboard KPIs: " + e.getMessage()));
        }
    }
    /**      * Get monthly revenue trend      */
    @GetMapping("/revenue-trend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getMonthlyRevenueTrend(
    @RequestParam(defaultValue = "6") int months) {
        try {
            if (months < 1 || months > 12) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Months must be between 1 and 12"));
            } Map<String, Object> trend = advancedAdminDashboardService.getMonthlyRevenueTrend(months);
            return ResponseEntity.ok(trend);
        } catch (Exception e) {
            return ResponseEntity.status(500) .body(Map.of("error", "Failed to get revenue trend: " + e.getMessage()));
        }
    }
    /**      * Get player engagement metrics      */
    @GetMapping("/player-engagement")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPlayerEngagementMetrics() {
        try {
            Map<String, Object> metrics = advancedAdminDashboardService.getPlayerEngagementMetrics();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            return ResponseEntity.status(500) .body(Map.of("error", "Failed to get player engagement metrics: " + e.getMessage()));
        }
    }
    /**      * Get payment aging report      */
    @GetMapping("/payment-aging")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPaymentAgingReport() {
        try {
            Map<String, Object> report = advancedAdminDashboardService.getPaymentAgingReport();
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(500) .body(Map.of("error", "Failed to get payment aging report: " + e.getMessage()));
        }
    }
    /**      * Get activity participation report      */
    @GetMapping("/activity-participation")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getActivityParticipationReport() {
        try {
            Map<String, Object> report = advancedAdminDashboardService.getActivityParticipationReport();
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(500) .body(Map.of("error", "Failed to get activity participation report: " + e.getMessage()));
        }
    }
    /**      * Get complete dashboard overview      */
    @GetMapping("/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getDashboardOverview() {
        try {
            Map<String, Object> overview = advancedAdminDashboardService.getDashboardOverview();
            return ResponseEntity.ok(overview);
        } catch (Exception e) {
            return ResponseEntity.status(500) .body(Map.of("error", "Failed to get dashboard overview: " + e.getMessage()));
        }
    }
}
