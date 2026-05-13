package com.footballacademy.controllers_rest.activity;

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
@RequestMapping("/api/activities")
public
class ActivityController {
    private final ActivityService activityService;
    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }
    @GetMapping
    public ResponseEntity<?> getAllActivities() {
        try {
            List<Activity> activities = activityService.getAllActivities();
            if (activities == null || activities.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            } return ResponseEntity.ok(activities);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to fetch activities: " + e.getMessage()));
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getActivityById(
    @PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Invalid activity ID"));
            } Activity activity = activityService.getActivityById(id);
            return ResponseEntity.ok(activity);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND) .body(Map.of("error", "Activity not found: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to fetch activity: " + e.getMessage()));
        }
    }
    @PostMapping
    public ResponseEntity<?> createActivity(
    @RequestBody Activity activity) {
        try {
            if (activity == null) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Activity data cannot be null"));
            }
            // Trainer month restriction
            try {
                var auth = org.springframework.security.core.context.SecurityContextHolder.getContext() .getAuthentication();
                if (auth != null) {
                    boolean isTrainer = auth.getAuthorities() != null && auth.getAuthorities() .stream() .anyMatch(a -> a.getAuthority() .equals("ROLE_TRAINER") || a.getAuthority() .equals("TRAINER"));
                    if (isTrainer) {
                        LocalDate d = LocalDate.parse(activity.getDate() .toString());
                        LocalDate now = LocalDate.now();
                        if (d.getYear() != now.getYear() || d.getMonthValue() != now.getMonthValue()) {
                            throw new RuntimeException("Trainer can only add activities for the current month");
                        }
                    }
                }
            } catch (Exception ex) {
                throw ex instanceof RuntimeException ?(RuntimeException) ex : new RuntimeException(ex.getMessage());
            } Activity createdActivity = activityService.createActivity(activity);
            return ResponseEntity.status(HttpStatus.CREATED) .body(createdActivity);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest() .body(Map.of("error", "Failed to create activity: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to create activity: " + e.getMessage()));
        }
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> updateActivity(
    @PathVariable Long id,
    @RequestBody Activity activity) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Invalid activity ID"));
            } Activity updatedActivity = activityService.updateActivity(id, activity);
            return ResponseEntity.ok(updatedActivity);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND) .body(Map.of("error", "Activity not found for update: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to update activity: " + e.getMessage()));
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteActivity(
    @PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Invalid activity ID"));
            } activityService.deleteActivity(id);
            return ResponseEntity.ok(Map.of("message", "Activity deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND) .body(Map.of("error", "Activity not found for deletion: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to delete activity: " + e.getMessage()));
        }
    }
    @PostMapping("/{id}/participants")
    public ResponseEntity<?> addParticipant(
    @PathVariable Long id,
    @RequestBody Long playerId) {
        try {
            if (id == null || id <= 0 || playerId == null || playerId <= 0) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Invalid activity ID or player ID"));
            } activityService.addParticipant(id, playerId);
            return ResponseEntity.ok(Map.of("message", "Participant added successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest() .body(Map.of("error", "Failed to add participant: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to add participant: " + e.getMessage()));
        }
    }
    @DeleteMapping("/{id}/participants/{playerId}")
    public ResponseEntity<?> removeParticipant(
    @PathVariable Long id,
    @PathVariable Long playerId) {
        try {
            if (id == null || id <= 0 || playerId == null || playerId <= 0) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Invalid activity ID or player ID"));
            } activityService.removeParticipant(id, playerId);
            return ResponseEntity.ok(Map.of("message", "Participant removed successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest() .body(Map.of("error", "Failed to remove participant: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to remove participant: " + e.getMessage()));
        }
    }
    @GetMapping("/date-range")
    public ResponseEntity<?> getActivitiesByDateRange(
    @RequestParam String start,
    @RequestParam String end) {
        try {
            LocalDate startDate = LocalDate.parse(start);
            LocalDate endDate = LocalDate.parse(end);
            List<Activity> activities = activityService.getActivitiesByDateRange(startDate, endDate);
            if (activities == null || activities.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            } return ResponseEntity.ok(activities);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest() .body(Map.of("error", "Failed to fetch activities by date range: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to fetch activities by date range: " + e.getMessage()));
        }
    }
    @GetMapping("/upcoming")
    public ResponseEntity<?> getUpcomingActivities() {
        try {
            List<Activity> activities = activityService.getUpcomingActivities();
            if (activities == null || activities.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            } return ResponseEntity.ok(activities);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to fetch upcoming activities: " + e.getMessage()));
        }
    }
}
