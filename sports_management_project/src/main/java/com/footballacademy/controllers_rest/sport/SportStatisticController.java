package com.footballacademy.controllers_rest.sport;

import com.footballacademy.model.SportStatistic;
import com.footballacademy.services.sport.SportStatisticService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sport-statistics")
public
class SportStatisticController {
    private final SportStatisticService sportStatisticService;
    public SportStatisticController(SportStatisticService sportStatisticService) {
        this.sportStatisticService = sportStatisticService;
    }
    @GetMapping
    public ResponseEntity<List<SportStatistic>> getAllStatistics() {
        return ResponseEntity.ok(sportStatisticService.getAllStatistics());
    }
    @GetMapping("/sport/{sportId}")
    public ResponseEntity<List<SportStatistic>> getStatisticsBySport(
    @PathVariable Long sportId) {
        return ResponseEntity.ok(sportStatisticService.getStatisticsBySport(sportId));
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getStatisticById(
    @PathVariable Long id) {
        return sportStatisticService.getStatisticById(id) .map(ResponseEntity::ok) .orElse(ResponseEntity.notFound() .build());
    }
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> createStatistic(
    @RequestBody SportStatistic statistic) {
        try {
            return ResponseEntity.status(201) .body(sportStatisticService.createStatistic(statistic));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest() .body(Map.of("error", e.getMessage()));
        }
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateStatistic(
    @PathVariable Long id,
    @RequestBody SportStatistic statistic) {
        try {
            return ResponseEntity.ok(sportStatisticService.updateStatistic(id, statistic));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest() .body(Map.of("error", e.getMessage()));
        }
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> deleteStatistic(
    @PathVariable Long id) {
        try {
            sportStatisticService.deleteStatistic(id);
            return ResponseEntity.ok(Map.of("message", "Statistic deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest() .body(Map.of("error", e.getMessage()));
        }
    }
}
