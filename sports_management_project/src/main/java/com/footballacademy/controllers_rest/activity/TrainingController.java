package com.footballacademy.controllers_rest.activity;

import com.footballacademy.model.Training;
import com.footballacademy.services.activity.TrainingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trainings")
public
class TrainingController {
    private final TrainingService trainingService;
    public TrainingController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }
    @GetMapping
    public ResponseEntity<?> getAllTrainings() {
        try {
            List<Training> trainings = trainingService.getAllTrainings();
            if (trainings == null || trainings.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            } return ResponseEntity.ok(trainings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to fetch trainings: " + e.getMessage()));
        }
    }
    @PostMapping("/{id}/record-attendance")
    public ResponseEntity<?> recordAttendance(
    @PathVariable Long id,
    @RequestBody List<Long> playerIds) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Invalid training ID"));
            }
            if (playerIds == null || playerIds.isEmpty()) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Player IDs cannot be empty"));
            } Training updatedTraining = trainingService.recordAttendance(id, playerIds);
            return ResponseEntity.ok(updatedTraining);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND) .body(Map.of("error", "Training not found: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to record attendance: " + e.getMessage()));
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getTrainingById(
    @PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Invalid training ID"));
            } Training training = trainingService.getTrainingById(id);
            return ResponseEntity.ok(training);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND) .body(Map.of("error", "Training not found: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to fetch training: " + e.getMessage()));
        }
    }
    @GetMapping("/trainer/{trainerId}")
    public ResponseEntity<?> getTrainingsByTrainer(
    @PathVariable Long trainerId) {
        try {
            if (trainerId == null || trainerId <= 0) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Invalid trainer ID"));
            } List<Training> trainings = trainingService.getTrainingsByTrainer(trainerId);
            if (trainings == null || trainings.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            } return ResponseEntity.ok(trainings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to fetch trainings by trainer: " + e.getMessage()));
        }
    }
    @GetMapping("/date-range")
    public ResponseEntity<?> getTrainingsInDateRange(
    @RequestParam String start,
    @RequestParam String end) {
        try {
            LocalDate startDate = LocalDate.parse(start);
            LocalDate endDate = LocalDate.parse(end);
            List<Training> trainings = trainingService.getTrainingsInDateRange(startDate, endDate);
            if (trainings == null || trainings.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            } return ResponseEntity.ok(trainings);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest() .body(Map.of("error", "Invalid date format or range: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to fetch trainings in date range: " + e.getMessage()));
        }
    }
    @PostMapping
    public ResponseEntity<?> createTraining(
    @RequestBody Training training) {
        try {
            if (training == null) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Training data cannot be null"));
            } Training createdTraining = trainingService.createTraining(training);
            return ResponseEntity.status(HttpStatus.CREATED) .body(createdTraining);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest() .body(Map.of("error", "Failed to create training: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to create training: " + e.getMessage()));
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTraining(
    @PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Invalid training ID"));
            } trainingService.deleteTraining(id);
            return ResponseEntity.ok(Map.of("message", "Training deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND) .body(Map.of("error", "Training not found for deletion: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to delete training: " + e.getMessage()));
        }
    }
}
