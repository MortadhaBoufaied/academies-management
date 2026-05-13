package com.footballacademy.controllers_rest.sport;

import com.footballacademy.model.SportPosition;
import com.footballacademy.services.sport.SportPositionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sport-positions")
public
class SportPositionController {
    private final SportPositionService sportPositionService;
    public SportPositionController(SportPositionService sportPositionService) {
        this.sportPositionService = sportPositionService;
    }
    @GetMapping
    public ResponseEntity<List<SportPosition>> getAllPositions() {
        return ResponseEntity.ok(sportPositionService.getAllPositions());
    }
    @GetMapping("/sport/{sportId}")
    public ResponseEntity<List<SportPosition>> getPositionsBySport(
    @PathVariable Long sportId) {
        return ResponseEntity.ok(sportPositionService.getPositionsBySport(sportId));
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getPositionById(
    @PathVariable Long id) {
        return sportPositionService.getPositionById(id) .map(ResponseEntity::ok) .orElse(ResponseEntity.notFound() .build());
    }
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> createPosition(
    @RequestBody SportPosition position) {
        try {
            return ResponseEntity.status(201) .body(sportPositionService.createPosition(position));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest() .body(Map.of("error", e.getMessage()));
        }
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> updatePosition(
    @PathVariable Long id,
    @RequestBody SportPosition position) {
        try {
            return ResponseEntity.ok(sportPositionService.updatePosition(id, position));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest() .body(Map.of("error", e.getMessage()));
        }
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> deletePosition(
    @PathVariable Long id) {
        try {
            sportPositionService.deletePosition(id);
            return ResponseEntity.ok(Map.of("message", "Position deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest() .body(Map.of("error", e.getMessage()));
        }
    }
}
