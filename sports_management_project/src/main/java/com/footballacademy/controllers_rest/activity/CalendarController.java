package com.footballacademy.controllers_rest.activity;

import com.footballacademy.config.AppUiProperties;
import com.footballacademy.model.Activity;
import com.footballacademy.services.activity.ActivityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/calendar")
public
class CalendarController {
    private final ActivityService activityService;
    private final AppUiProperties appUiProperties;
    public CalendarController(ActivityService activityService, AppUiProperties appUiProperties) {
        this.activityService = activityService;
        this.appUiProperties = appUiProperties;
    }
    @GetMapping("/month/{year}/{month}")
    public ResponseEntity<?> getActivitiesForMonth(
    @PathVariable int year,
    @PathVariable int month) {
        try {
            int minAllowedYear = appUiProperties.getPayments() .getMinAllowedYear();
            int maxAllowedYear = appUiProperties.getPayments() .getMaxAllowedYear();
            if (month < 1 || month > 12 || year < minAllowedYear || year > maxAllowedYear) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Invalid year or month parameters"));
            } LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
            List<Activity> activities = activityService.getActivitiesByDateRange(startDate, endDate);
            if (activities == null || activities.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            } return ResponseEntity.ok(activities);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to fetch activities for month: " + e.getMessage()));
        }
    }
    @GetMapping("/day/{date}")
    public ResponseEntity<?> getActivitiesForDay(
    @PathVariable String date) {
        try {
            LocalDate day = LocalDate.parse(date);
            List<Activity> activities = activityService.getActivitiesByDateRange(day, day);
            if (activities == null || activities.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            } return ResponseEntity.ok(activities);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest() .body(Map.of("error", "Invalid date format or date: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to fetch activities for day: " + e.getMessage()));
        }
    }
}
