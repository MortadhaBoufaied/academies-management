package com.footballacademy.controllers_rest.trainer;

import com.footballacademy.DTO.TrainerCombinedDTO;
import com.footballacademy.model.Activity;
import com.footballacademy.model.Player;
import com.footballacademy.model.Trainer;
import com.footballacademy.services.trainer.TrainerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trainers")
public
class TrainerController {
    private final TrainerService trainerService;
    public TrainerController(TrainerService trainerService) {
        this.trainerService = trainerService;
    }
    @GetMapping
    public ResponseEntity<?> getAllTrainers() {
        try {
            List<TrainerCombinedDTO> trainers = trainerService.getAllTrainersCombined();
            if (trainers == null || trainers.isEmpty()) return ResponseEntity.ok(Collections.emptyList());
            return ResponseEntity.ok(trainers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to fetch trainers: " + e.getMessage()));
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getTrainerById(
    @PathVariable Long id) {
        try {
            return ResponseEntity.ok(trainerService.getTrainerCombinedById(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND) .body(Map.of("error", e.getMessage()));
        }
    }
    @GetMapping("/speciality/{speciality}")
    public ResponseEntity<?> getTrainersBySpeciality(
    @PathVariable String speciality) {
        return ResponseEntity.ok(trainerService.getTrainersBySpeciality(speciality));
    }
    @PutMapping("/{id}/division")
    public ResponseEntity<?> assignDivision(
    @PathVariable Long id,
    @RequestBody Map<String, Object> body) {
        Long divisionId = body.get("divisionId") == null ? null : Long.valueOf(body.get("divisionId") .toString());
        Trainer updated = trainerService.assignDivision(id, divisionId);
        return ResponseEntity.ok(updated);
    }
    @GetMapping("/{trainerId}/players")
    public ResponseEntity<?> getTrainerPlayers(
    @PathVariable Long trainerId) {
        List<Player> players = trainerService.getTrainerPlayers(trainerId);
        return ResponseEntity.ok(players);
    }
    @GetMapping("/{trainerId}/activities")
    public ResponseEntity<?> getTrainerActivities(
    @PathVariable Long trainerId) {
        List<Activity> activities = trainerService.getTrainerActivities(trainerId);
        return ResponseEntity.ok(activities);
    }
    @PostMapping("/{trainerId}/activities")
    public ResponseEntity<?> planActivity(
    @PathVariable Long trainerId,
    @RequestBody Activity activity) {
        Activity created = trainerService.planActivity(trainerId, activity);
        return ResponseEntity.status(HttpStatus.CREATED) .body(created);
    }
    @PostMapping("/{trainerId}/players/{playerId}")
    public ResponseEntity<?> assignPlayer(
    @PathVariable Long trainerId,
    @PathVariable Long playerId) {
        try {
            trainerService.assignPlayerToTrainer(trainerId, playerId);
            return ResponseEntity.ok(Map.of("message", "Player assigned to trainer successfully", "trainerId", trainerId, "playerId", playerId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND) .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST) .body(Map.of("error", e.getMessage()));
        }
    }
    // NEW: list divisions coached by a trainer (multi-division)
    @GetMapping("/{trainerId}/divisions")
    public ResponseEntity<?> getTrainerDivisions(
    @PathVariable Long trainerId) {
        var divs = trainerService.getTrainerDivisions(trainerId);
        var out = divs.stream() .map(d -> java.util.Map.of("id", d.getId(), "nom", d.getNom(), "category", d.getCategorie(), "categorie", d.getCategorie(), "sportId", d.getSport() != null ? d.getSport() .getId() : null, "sportName", d.getSport() != null ? d.getSport() .getName() : null)) .toList();
        return ResponseEntity.ok(out);
    }
    // NEW: assign multiple divisions
    @PutMapping("/{trainerId}/divisions")
    public ResponseEntity<?> assignTrainerDivisions(
    @PathVariable Long trainerId,
    @RequestBody java.util.List<Long> divisionIds) {
        var updated = trainerService.assignDivisions(trainerId, divisionIds);
        return ResponseEntity.ok(updated);
    }
}
