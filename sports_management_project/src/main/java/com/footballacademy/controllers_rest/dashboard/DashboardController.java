package com.footballacademy.controllers_rest.dashboard;

import com.footballacademy.DTO.dashboard.*;
import com.footballacademy.services.dashboard.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public
class DashboardController {
    private final DashboardService dashboardService;
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }
    @GetMapping("/parent/{parentId}")
    public ResponseEntity<ParentDashboardDTO> parent(
    @PathVariable Long parentId) {
        return ResponseEntity.ok(dashboardService.parentDashboard(parentId));
    }
    @GetMapping("/trainer/{trainerId}")
    public ResponseEntity<TrainerDashboardDTO> trainer(
    @PathVariable Long trainerId) {
        return ResponseEntity.ok(dashboardService.trainerDashboard(trainerId));
    }
    @GetMapping("/admin")
    public ResponseEntity<AdminDashboardDTO> admin() {
        return ResponseEntity.ok(dashboardService.adminDashboard());
    }
}
