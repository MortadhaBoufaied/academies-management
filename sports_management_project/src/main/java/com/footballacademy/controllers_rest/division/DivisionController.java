package com.footballacademy.controllers_rest.division;

import com.footballacademy.model.Division;
import com.footballacademy.services.division.DivisionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/divisions")
public
class DivisionController {
    private final DivisionService divisionService;
    public DivisionController(DivisionService divisionService) {
        this.divisionService = divisionService;
    }
    @GetMapping
    public ResponseEntity<?> getAllDivisions() {
        try {
            return ResponseEntity.ok(divisionService.getAllDivisions());
        } catch (Exception e) {
            return ResponseEntity.status(500) .body(Map.of("error", e.getMessage()));
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getDivisionById(
    @PathVariable Long id) {
        try {
            return ResponseEntity.ok(divisionService.getDivisionById(id));
        } catch (Exception e) {
            return ResponseEntity.status(404) .body(Map.of("error", e.getMessage()));
        }
    }
    @PostMapping
    public ResponseEntity<?> createDivision(
    @RequestBody Division division) {
        try {
            return ResponseEntity.status(201) .body(divisionService.createDivision(division));
        } catch (Exception e) {
            return ResponseEntity.badRequest() .body(Map.of("error", e.getMessage()));
        }
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDivision(
    @PathVariable Long id,
    @RequestBody Division division) {
        try {
            return ResponseEntity.ok(divisionService.updateDivision(id, division));
        } catch (Exception e) {
            return ResponseEntity.status(404) .body(Map.of("error", e.getMessage()));
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDivision(
    @PathVariable Long id) {
        try {
            divisionService.deleteDivision(id);
            return ResponseEntity.ok(Map.of("message", "Deleted"));
        } catch (Exception e) {
            return ResponseEntity.status(404) .body(Map.of("error", e.getMessage()));
        }
    }
    @PostMapping("/{divisionId}/players")
    public ResponseEntity<?> addPlayerToDivision(
    @PathVariable Long divisionId,
    @RequestBody Object body) {
        try {
            Long playerId = null;
            if (body instanceof Number n) {
                playerId = n.longValue();
            } else
            if (body instanceof String s) {
                playerId = Long.parseLong(s.trim());
            } else
            if (body instanceof Map<?, ?> m && m.get("playerId") != null) {
                Object value = m.get("playerId");
                if (value instanceof Number n) playerId = n.longValue();
                else playerId = Long.parseLong(String.valueOf(value));
            }
            if (playerId == null || playerId <= 0) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Valid playerId is required"));
            } divisionService.addPlayer(divisionId, playerId);
            return ResponseEntity.status(HttpStatus.CREATED) .body(Map.of("message", "Player added to division", "divisionId", divisionId, "playerId", playerId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST) .body(Map.of("error", e.getMessage()));
        }
    }
    @DeleteMapping("/{divisionId}/players/{playerId}")
    public ResponseEntity<?> removePlayerFromDivision(
    @PathVariable Long divisionId,
    @PathVariable Long playerId) {
        try {
            divisionService.removePlayer(divisionId, playerId);
            return ResponseEntity.ok(Map.of("message", "Player removed from division"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST) .body(Map.of("error", e.getMessage()));
        }
    }
    @GetMapping({
        "/category/{category}", "/categorie/{category}"
    })
    public ResponseEntity<?> getDivisionsByCategory(
    @PathVariable String category) {
        try {
            List<Division> divisions = divisionService.getDivisionsByCategory(category);
            return ResponseEntity.ok(divisions != null ? divisions : Collections.emptyList());
        } catch (Exception e) {
            return ResponseEntity.status(500) .body(Map.of("error", e.getMessage()));
        }
    }
    @GetMapping("/sport/{sportId}")
    public ResponseEntity<?> getDivisionsBySport(
    @PathVariable Long sportId) {
        try {
            return ResponseEntity.ok(divisionService.getDivisionsBySport(sportId));
        } catch (Exception e) {
            return ResponseEntity.status(500) .body(Map.of("error", e.getMessage()));
        }
    }
}
